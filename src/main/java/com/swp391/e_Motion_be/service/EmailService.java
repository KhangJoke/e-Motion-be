package com.swp391.e_Motion_be.service;

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

    public void sendEmail(String to, String subject, String htmlMessage) {
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



    // format pickup time: e.g. "01 Oct 2025, 14:30"
    public String buildReservationHtml(Reservation reservation, String header, String message) {
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
