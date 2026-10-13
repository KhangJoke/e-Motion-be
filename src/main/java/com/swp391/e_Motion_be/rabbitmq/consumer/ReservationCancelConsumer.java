package com.swp391.e_Motion_be.rabbitmq.consumer;

import com.swp391.e_Motion_be.config.RabbitMQConfig;
import com.swp391.e_Motion_be.dto.message.ReservationTimeoutMessage;
import com.swp391.e_Motion_be.entity.Deposit;
import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.repository.DepositRepository;
import com.swp391.e_Motion_be.repository.ReservationRepository;
import com.swp391.e_Motion_be.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCancelConsumer {

    private final ReservationRepository reservationRepository;
    private final DepositRepository depositRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Lắng nghe message dead-letter từ RabbitMQ sau khi hết 15 phút TTL giữ chỗ
     */
    @RabbitListener(queues = RabbitMQConfig.CANCEL_QUEUE)
    @Transactional
    public void handleReservationHoldTimeout(ReservationTimeoutMessage message) {
        if (message == null || message.getReservationId() == null) {
            log.warn("Received null or empty reservation timeout message");
            return;
        }

        Long reservationId = message.getReservationId();
        String code = message.getReservationCode();
        log.info("Processing RabbitMQ reservation hold timeout for reservation id={}, code={}", reservationId, code);

        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            log.warn("Reservation id={} not found for auto-cancellation", reservationId);
            return;
        }

        // Chỉ tự động hủy nếu đơn vẫn đang ở trạng thái PENDING (chưa cọc thành công)
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            log.info("Reservation #{} is still PENDING after 15 minutes. Proceeding to AUTO-CANCEL...", reservation.getCode());

            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);

            // Cập nhật trạng thái Deposit thành FAILED nếu chưa thanh toán
            Deposit deposit = reservation.getDeposit();
            if (deposit == null) {
                deposit = depositRepository.findByReservation_Id(reservation.getId()).orElse(null);
            }
            if (deposit != null && deposit.getStatus() == DepositStatus.PENDING) {
                deposit.setStatus(DepositStatus.FAILED);
                depositRepository.save(deposit);
            }

            // Giải phóng khóa giữ chỗ trong Redis
            try {
                String reservationKey = "reservation:" + reservation.getId();
                redisTemplate.delete(reservationKey);

                if (reservation.getVehicle() != null) {
                    String vehicleKey = "vehicle:" + reservation.getVehicle().getId();
                    redisTemplate.delete(vehicleKey);
                    log.info("Released Redis vehicle lock key: {}", vehicleKey);
                }
            } catch (Exception e) {
                log.warn("Error releasing Redis keys for auto-cancelled reservation #{}: {}", reservation.getCode(), e.getMessage());
            }

            // Gửi email thông báo đơn đã bị hủy vì quá hạn thanh toán
            try {
                emailService.sendReservationCancelEmail(reservation);
                log.info("Sent cancellation notification email for reservation #{}", reservation.getCode());
            } catch (Exception e) {
                log.error("Failed to send cancellation email for reservation #{}: {}", reservation.getCode(), e.getMessage());
            }

            log.info("Reservation #{} (id={}) successfully AUTO-CANCELLED by RabbitMQ delayed worker.", reservation.getCode(), reservation.getId());
        } else {
            log.info("Reservation #{} (id={}) has status {}, skipping RabbitMQ auto-cancellation.",
                    reservation.getCode(), reservation.getId(), reservation.getStatus());
        }
    }
}
