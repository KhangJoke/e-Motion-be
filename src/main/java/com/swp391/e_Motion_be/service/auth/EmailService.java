package com.swp391.e_Motion_be.service.auth;

import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        emailSender.send(mimeMessage);
    }

    public void sendReservationCodeEmail(Reservation reservation) {
        String subject = "Your Reservation Code";
        String reservationCode = reservation.getCode();

        // format pickup time: e.g. "01 Oct 2025, 14:30"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String pickupTime = reservation.getStartTime() != null
                ? reservation.getStartTime().format(formatter)
                : "Not specified";

        String htmlMessage = "<html style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f9f9f9; padding: 20px;\">"
                + "<h2 style=\"color: #2c3e50;\">Reservation Confirmed ✅</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">"
                + "Thank you for using our service. Please keep the reservation code below safe:</p>"
                + "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 8px; "
                + "border: 1px solid #ddd; margin: 20px 0; text-align: center;\">"
                + "<h3 style=\"color: #333; margin-bottom: 10px;\">Your Reservation Code</h3>"
                + "<p style=\"font-size: 22px; font-weight: bold; color: #007bff; letter-spacing: 2px;\">"
                + reservationCode + "</p>"
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

        try {
            sendVerificationEmail(reservation.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }
}
