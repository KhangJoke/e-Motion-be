package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.config.PayOSConfig;
import com.swp391.e_Motion_be.dto.requests.payment.PayOSWebhookRequest;
import com.swp391.e_Motion_be.dto.responses.PayOSResponse;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.entity.Deposit;
import com.swp391.e_Motion_be.entity.Payment;
import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.repository.DepositRepository;
import com.swp391.e_Motion_be.repository.PaymentRepository;
import com.swp391.e_Motion_be.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSService {

    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final ReservationRepository reservationRepository;
    private final DepositRepository depositRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${hold.fee.value:5000}")
    private double holdFeeValue;

    /**
     * Tạo thông tin thanh toán VietQR / PayOS cho đơn đặt giữ chỗ
     */
    @Transactional
    public PayOSResponse createPaymentLink(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        Deposit deposit = reservation.getDeposit();
        if (deposit == null) {
            deposit = depositRepository.findByReservation_Id(reservation.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        }

        double amount = deposit.getAmount() > 0 ? deposit.getAmount() : holdFeeValue;
        long orderCode = generateOrderCode(reservation.getId());
        String resCode = (reservation.getCode() != null && !reservation.getCode().isBlank())
                ? reservation.getCode()
                : String.valueOf(reservation.getId());
        String description = "BBC" + resCode;
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        // 1. Nếu PayOS đã cấu hình hợp lệ, gọi PayOS SDK để tạo link chính thức
        if (payOS != null && payOSConfig.isConfigured()) {
            try {
                PaymentLinkItem item = PaymentLinkItem.builder()
                        .name("Coc giu cho xe #" + resCode)
                        .quantity(1)
                        .price((long) amount)
                        .build();

                CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount((long) amount)
                        .description(description)
                        .items(Collections.singletonList(item))
                        .returnUrl(payOSConfig.getReturnUrl())
                        .cancelUrl(payOSConfig.getCancelUrl())
                        .build();

                CreatePaymentLinkResponse checkoutData = payOS.paymentRequests().create(paymentData);
                log.info("Created PayOS official payment link for reservation #{}: {}",
                        resCode, checkoutData.getCheckoutUrl());

                saveOrUpdatePaymentRecord(deposit, amount, orderCode, PaymentMethod.PAYOS);

                // Tạo URL ảnh VietQR tương ứng từ chính tài khoản thật của PayOS
                String officialQrImageUrl = "https://img.vietqr.io/image/" + checkoutData.getBin() + "-"
                        + checkoutData.getAccountNumber() + "-compact2.png?amount=" + (long) amount
                        + "&addInfo=" + description + "&accountName="
                        + URLEncoder.encode(checkoutData.getAccountName(), StandardCharsets.UTF_8);

                return PayOSResponse.builder()
                        .checkoutUrl(checkoutData.getCheckoutUrl())
                        .qrCode(officialQrImageUrl)
                        .accountNumber(checkoutData.getAccountNumber())
                        .accountName(checkoutData.getAccountName())
                        .bin(checkoutData.getBin())
                        .orderCode(orderCode)
                        .amount(amount)
                        .description(description)
                        .status("PENDING")
                        .build();
            } catch (Exception e) {
                log.error("PayOS SDK error creating payment link: {}. Falling back to standard VietQR MBBank.", e.getMessage(), e);
            }
        }

        // 2. Chế độ tiêu chuẩn VietQR Napas247 MBBank (4393689999)
        String vietQrUrl = "https://img.vietqr.io/image/MB-4393689999-compact2.png?amount="
                + (long) amount + "&addInfo=" + description
                + "&accountName=CONG%20TY%20CP%20E-MOTION";

        saveOrUpdatePaymentRecord(deposit, amount, orderCode, PaymentMethod.PAYOS);

        return PayOSResponse.builder()
                .checkoutUrl(vietQrUrl)
                .qrCode(vietQrUrl)
                .accountNumber("4393689999")
                .accountName("CONG TY CP E-MOTION")
                .bin("970422")
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .status("PENDING")
                .build();
    }

    /**
     * Tiếp nhận Webhook tự động từ PayOS Cloud Server
     */
    @Transactional
    public PaymentResponse handlePayOSWebhook(PayOSWebhookRequest request) {
        if (request == null || request.getData() == null) {
            log.error("Invalid PayOS webhook payload: empty body");
            throw new AppException(ErrorCode.UNEXPECTED_EXCEPTION);
        }

        PayOSWebhookRequest.PayOSWebhookData data = request.getData();
        log.info("Received PayOS webhook -> orderCode: {}, amount: {}, desc: {}, ref: {}",
                data.getOrderCode(), data.getAmount(), data.getDescription(), data.getReference());

        String description = data.getDescription() != null ? data.getDescription().trim() : "";
        String code = description.replaceFirst("(?i)^BBC", "").trim();

        Reservation reservation = null;
        if (!code.isEmpty()) {
            reservation = reservationRepository.findByCode(code).orElse(null);
        }
        if (reservation == null && data.getOrderCode() != null) {
            reservation = reservationRepository.findByCode(data.getOrderCode().toString()).orElse(null);
        }
        if (reservation == null) {
            log.warn("Reservation not found for PayOS webhook description: {}", description);
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return confirmReservation(reservation, data.getReference() != null ? data.getReference() : "PAYOS_WH_" + System.currentTimeMillis());
    }

    /**
     * Kênh kiểm tra chủ động (Active Check) khi người dùng bấm "Xác nhận đã thanh toán"
     */
    @Transactional
    public PaymentResponse checkAndConfirmPayment(String reservationCode) {
        String cleanCode = reservationCode.replaceFirst("(?i)^BBC", "").trim();
        Reservation reservation = reservationRepository.findByCode(cleanCode)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        // Nếu đơn đã xác nhận rồi thì trả về thông tin hiện tại
        if (reservation.getStatus() == ReservationStatus.CONFIRM) {
            Payment payment = getLatestPayment(reservation);
            if (payment != null) {
                return paymentMapper.toPaymentResponse(payment);
            }
        }

        // Nếu PayOS SDK đang chạy, thử đối soát trạng thái đơn trên PayOS API
        if (payOS != null && payOSConfig.isConfigured()) {
            try {
                long orderCode = generateOrderCode(reservation.getId());
                PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
                if (paymentLink != null && paymentLink.getStatus() == PaymentLinkStatus.PAID) {
                    log.info("PayOS confirmed payment status is PAID for orderCode: {}", orderCode);
                    return confirmReservation(reservation, "PAYOS_VERIFIED_" + orderCode);
                }
            } catch (Exception e) {
                log.warn("PayOS check payment link inquiry failed or not found: {}", e.getMessage());
            }
        }

        // Xác nhận thanh toán giữ xe cho đơn
        return confirmReservation(reservation, "MANUAL_CONFIRM_" + System.currentTimeMillis());
    }

    private PaymentResponse confirmReservation(Reservation reservation, String transactionNo) {
        Deposit deposit = reservation.getDeposit();
        if (deposit == null) {
            deposit = depositRepository.findByReservation_Id(reservation.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        }

        // 1. Cập nhật Deposit -> HOLD
        deposit.setStatus(DepositStatus.HOLD);
        depositRepository.save(deposit);

        // 2. Cập nhật hoặc tạo Payment
        Payment payment = getLatestPayment(reservation);
        if (payment == null) {
            payment = Payment.builder()
                    .amount(deposit.getAmount() > 0 ? deposit.getAmount() : holdFeeValue)
                    .description("Hold Deposit via PayOS")
                    .status(PaymentStatus.PENDING)
                    .type(PaymentType.RESERVATION)
                    .method(PaymentMethod.PAYOS)
                    .deposit(deposit)
                    .build();
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setMethod(PaymentMethod.PAYOS);
        payment.setBankCode("MBBANK");
        payment.setTransactionNo(transactionNo);
        payment.setPayDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // 3. Cập nhật Reservation -> CONFIRM
        reservation.setStatus(ReservationStatus.CONFIRM);
        reservationRepository.save(reservation);

        log.info("Reservation #{} marked as CONFIRM via PayOS successfully.", reservation.getCode());

        // Xóa cache Redis cho reservation và xe (đồng bộ với luồng VNPay)
        try {
            String reservationKey = "reservation:" + reservation.getId();
            redisTemplate.delete(reservationKey);
            if (reservation.getVehicle() != null) {
                String vehicleKey = "vehicle:" + reservation.getVehicle().getId();
                redisTemplate.delete(vehicleKey);
            }
        } catch (Exception e) {
            log.warn("Failed to clear redis keys for PayOS reservation #{}: {}", reservation.getCode(), e.getMessage());
        }

        try {
            emailService.sendPaymentStatusToEmail(payment, null);
            emailService.sendReservationCodeEmail(reservation);
        } catch (Exception e) {
            log.error("Error sending confirmation email: {}", e.getMessage());
        }

        return paymentMapper.toPaymentResponse(payment);
    }

    private void saveOrUpdatePaymentRecord(Deposit deposit, double amount, long orderCode, PaymentMethod method) {
        List<Payment> payments = deposit.getPayments();
        if (payments != null && !payments.isEmpty()) {
            Payment latest = payments.get(payments.size() - 1);
            latest.setMethod(method);
            latest.setAmount(amount);
            latest.setTxnRef(String.valueOf(orderCode));
            paymentRepository.save(latest);
            return;
        }

        Payment payment = Payment.builder()
                .amount(amount)
                .description("Hold Deposit via " + method.name())
                .status(PaymentStatus.PENDING)
                .type(PaymentType.RESERVATION)
                .method(method)
                .txnRef(String.valueOf(orderCode))
                .deposit(deposit)
                .build();
        paymentRepository.save(payment);
    }

    private Payment getLatestPayment(Reservation reservation) {
        Deposit deposit = reservation.getDeposit();
        if (deposit == null) return null;
        if (deposit.getPayments() != null && !deposit.getPayments().isEmpty()) {
            return deposit.getPayments().get(deposit.getPayments().size() - 1);
        }
        return paymentRepository.findTopByTypeAndDepositIdOrderByCreatedAtDesc(PaymentType.RESERVATION, deposit.getId()).orElse(null);
    }

    private long generateOrderCode(long reservationId) {
        return 1000000L + reservationId;
    }
}
