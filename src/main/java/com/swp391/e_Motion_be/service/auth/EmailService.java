package com.swp391.e_Motion_be.service.auth;

import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.ReservationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final ReservationRepository reservationRepository;

    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        emailSender.send(mimeMessage);
    }

    private void sendEmail(String to, String subject, String htmlMessage) {
        try {
            sendVerificationEmail(to, subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
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


    // format pickup time: e.g. "01 Oct 2025, 14:30"
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
                + "<p style=\"font-size: 15px; color: #444;\"><strong>Pickup Time:</strong> "
                + pickupTime + "</p>"
                + "<p style=\"font-size: 15px; color: #444;\">"
                + "👉 Please go to the station and provide this code to our staff to proceed with your rental."
                + "</p>"
                + "</p>"
                + "</div>"
                + "</html>";
    }
}
