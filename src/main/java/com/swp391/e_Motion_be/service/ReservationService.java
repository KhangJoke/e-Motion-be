package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.UpdateReservationStatusRequest;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.ReservationResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.mapper.ReservationMapper;
import com.swp391.e_Motion_be.repository.*;
import com.swp391.e_Motion_be.service.auth.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final DepositRepository depositRepository;

    public ReservationResponse createReservation(CreateReservationRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if(!request.getEndTime().isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RESERVATION_ENDTIME_INVALID);
        }
        Reservation reservation = reservationMapper.toReservationEntity(request);
        if(reservation.getStartTime().isBefore(LocalDateTime.now()) ||
                reservation.getEndTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RESERVATION_TIME_INVALID);
        }
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setStation(station);
        reservation.setCode(generateCode());
        reservationRepository.save(reservation);
        sendReservationCodeEmail(reservation);

        return reservationMapper.toReservationResponse(reservation);
    }

    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public ReservationResponse getReservationByCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        return reservationMapper.toReservationResponse(reservation);
    }

    public ReservationResponse updateReservationStatus(UpdateReservationStatusRequest request) {
        Reservation reservation = reservationRepository.findByCode(request.getReservationCode())
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.setStatus(request.getNewStatus());

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toReservationResponse(savedReservation);
    }

    public void deleteReservationByCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        reservationRepository.delete(reservation);
    }

    @Transactional
    public boolean cancelReservation(String code) throws Exception {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        //Nếu đã hết hạn thì không cho huỷ (Truoc 5 ngay)
        if(reservation.getStartTime().isBefore(LocalDateTime.now().plusDays(5))) {
            throw new AppException(ErrorCode.RESERVATION_TIME_INVALID_TO_CANCEL);
        }

        //Nếu đã huỷ rồi thì không cho huỷ
        if(reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new AppException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        if(reservation.getDeposit()!=null) {
            Deposit deposit = reservation.getDeposit();
            deposit.setStatus(DepositStatus.RELEASED);
            depositRepository.save(deposit);
            Payment depositPayment = paymentRepository.findByDepositIdAndType(deposit.getId(), PaymentType.RESERVATION)
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_PAYMENT_NOT_FOUND));

            RefundRequest refundRequest = paymentMapper.toRefundRequest(depositPayment);

            PaymentResponse refundResponse = paymentService.refundPayment(refundRequest);
            return refundResponse != null;
        }
        return false;
    }

    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatus(status);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getReservationsByUserEmail(String email) {
        List<Reservation> reservations = reservationRepository.findByUserEmail(email);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getReservationsByStationName(String name) {
        List<Reservation> reservations = reservationRepository.findByStationName(name);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getReservationsByVehicleId(Long vehicleId) {
        List<Reservation> reservations = reservationRepository.findByVehicleId(vehicleId);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getValidReservations(LocalDateTime time) {
        List<Reservation> reservations = reservationRepository.findByEndTimeBefore(time);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    private String generateCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public void sendReservationCodeEmail(Reservation reservation) {
        String subject = "Your Reservation Code";
        String message = "Thanks for using our service.";
        String htmlMessage = buildReservationHtml(reservation, subject, message);
        sendEmail(reservation.getUser().getEmail(), subject, htmlMessage);
    }

    @Scheduled(fixedRate = 86400000) // every day
    public void notifyReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> reservations = reservationRepository.findByStatusWithUser(
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRM)
        );
        // or optimized query

        String subject;
        String htmlMessage;

        for (Reservation reservation : reservations) {
            try {
                LocalDateTime startTime = reservation.getStartTime();
                ReservationStatus status = reservation.getStatus();

                // Pending reservations flow
                if (status == ReservationStatus.PENDING) {
                    if (startTime.isBefore(now.plusDays(14)) && startTime.isAfter(now.plusDays(7))) {
                        subject = "📝 Please Confirm Your Reservation";
                        htmlMessage = buildReservationHtml(reservation, subject,
                                "Your reservation is pending. Please confirm it within the next 7 days.");
                        sendEmail(reservation.getUser().getEmail(), subject, htmlMessage);
                    } else if (startTime.isBefore(now.plusDays(7))) {
                        subject = "❌ Your Reservation Has Expired";
                        htmlMessage = buildReservationHtml(reservation, subject,
                                "Your pending reservation has expired as you did not confirm in time.");
                        sendEmail(reservation.getUser().getEmail(), subject, htmlMessage);

                        reservation.setStatus(ReservationStatus.EXPIRED);
                        reservationRepository.save(reservation);
                    }
                }

                // Confirmed reservations flow
                else if (status == ReservationStatus.CONFIRM) {
                    if (startTime.isBefore(now.plusDays(3)) && startTime.isAfter(now)) {
                        subject = "⏰ Your Reservation is Coming Up Soon!";
                        htmlMessage = buildReservationHtml(reservation, subject,
                                "Your confirmed reservation is approaching. Get ready!");
                        sendEmail(reservation.getUser().getEmail(), subject, htmlMessage);
                    } else if (startTime.isBefore(now)) {
                        subject = "⚠️ Your Reservation is Late/Expired!";
                        htmlMessage = buildReservationHtml(reservation, subject,
                                "Your reservation time has passed. Please contact support if needed.");
                        sendEmail(reservation.getUser().getEmail(), subject, htmlMessage);

                        reservation.setStatus(ReservationStatus.EXPIRED);
                        reservationRepository.save(reservation);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void sendEmail(String to, String subject, String htmlMessage) {
        try {
            emailService.sendVerificationEmail(to, subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    private String buildReservationHtml(Reservation reservation, String header, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String pickupTime = reservation.getStartTime() != null
                ? reservation.getStartTime().format(formatter)
                : "Not specified";

        return "<html style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f9f9f9; padding: 20px;\">"
                + "<h2 style=\"color: #2c3e50;\">" + header + "</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">" + message + "</p>"
                    + "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 8px; "
                    + "border: 1px solid #ddd; margin: 20px 0; text-align: center;\">"
                    + "<h3 style=\"color: #333; margin-bottom: 10px;\">Your Reservation Code</h3>"
                    + "<p style=\"font-size: 22px; font-weight: bold; color: #007bff; letter-spacing: 2px;\">"
                    + reservation.getCode() + "</p>"
                    + "</div>"
                + "<p style=\"font-size: 15px; color: #444;\">"
                + "👉 Please go to the station and provide this code to our staff to proceed with your rental."
                + "</p>"
                + "<p style=\"font-size: 15px; color: #444; margin-top: 20px;\">"
                + "<strong>Pickup Time:</strong> " + pickupTime + "<br>"
                + "<strong>Cancellation Policy:</strong> You may cancel your reservation up to "
                + "<span style=\"color:#e74c3c; font-weight:bold;\">5 days before</span> your trip."
                + "</p>"
                + "<p style=\"font-size: 13px; color: #999; margin-top: 30px;\">"
                + "If you did not make this reservation, please ignore this email."
                + "</p>"
                + "</div>"
                + "</html>";
    }


}
