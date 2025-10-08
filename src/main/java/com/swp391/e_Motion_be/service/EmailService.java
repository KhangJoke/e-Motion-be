package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.payment.PaymentEmailRequest;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.ReservationRepository;
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
                + "Please go to the station and provide this code to our staff to proceed with your rental."
                + "</p>"
                + "</p>"
                + "</div>"
                + "</html>";
    }

    public void sendPaymentStatusToEmail(Payment payment) {
        String subject = "Your Payment Confirmation";
        String message = "Thanks for your payment! Below are your transaction details.";

        Rental rental = null;
        Deposit deposit = null;
        RentalCheckList rentalCheckList = null;
        VehicleLog vehicleLog = null;

        if(payment.getType() == PaymentType.RENTAL) {
            rental = payment.getRental();
            deposit = payment.getDeposit();
        }
        else if(payment.getType() == PaymentType.RESERVATION) {
            deposit = payment.getDeposit();
        }
        else if(payment.getType() == PaymentType.PENALTY_FEE_RENTAL) {
            rental = payment.getRental();
            rentalCheckList = payment.getRental().getRentalCheckLists().stream()
                    .filter(r -> r.getType().toString().equalsIgnoreCase(CheckType.CHECK_OUT.toString()))
                    .findFirst()
                    .orElse(null);
            vehicleLog = payment.getRental().getVehicleLog();
            deposit = payment.getDeposit();
        }

        PaymentEmailRequest paymentEmailRequest = PaymentEmailRequest.builder()
                .userEmail(payment.getUser().getEmail())
                .payment(payment)
                .deposit(deposit)
                .rental(rental)
                .rentalCheckList(rentalCheckList)
                .vehicleLog(vehicleLog)
                .build();
        String htmlMessage = buildPaymentHtml(paymentEmailRequest , subject, message);
        sendEmail(payment.getUser().getEmail(), subject, htmlMessage);
    }

    private String buildPaymentHtml(PaymentEmailRequest request, String subject, String message) {
        StringBuilder html = new StringBuilder();

        double depositFee = 0;
        double rentalFee = 0;
        double penaltyFee = 0;
        double total = 0;

        if (request.getDeposit() != null && request.getDeposit().getAmount() > 0) {
            depositFee = request.getDeposit().getAmount();
            total += depositFee;
        }
        if (request.getRental() != null && request.getRental().getRentFee() > 0) {
            rentalFee = request.getRental().getRentFee();
            total += rentalFee;
        }
        if (request.getRentalCheckList() != null && request.getRentalCheckList().getFee() > 0) {
            penaltyFee = request.getRentalCheckList().getFee();
            total += penaltyFee;
        }

        // Lấy trạng thái thanh toán
        String paymentStatus = "DEFAULT";
        if (request.getPayment() != null && request.getPayment().getStatus() != null) {
            paymentStatus = request.getPayment().getStatus().toString();
        }

        // Màu status hiển thị
        String statusColor;
        switch (paymentStatus.toUpperCase()) {
            case "SUCCESS":
                statusColor = "#38a169"; // xanh lá
                break;
            case "FAILED":
                statusColor = "#e53e3e"; // đỏ
                break;
            default:
                statusColor = "#718096"; // xám
                break;
        }
        html.append("<html>");
        html.append("<body style='font-family: Arial, sans-serif; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; padding: 20px;'>");
        html.append("<h2 style='color: #2b6cb0;'>").append(subject).append("</h2>");
        html.append("<p>").append(message).append("</p>");

        // Payment status
        html.append("<p style='font-size:16px;'><b>Status:</b> ")
                .append("<span style='color:").append(statusColor).append(";'>")
                .append(paymentStatus)
                .append("</span></p>");
        html.append("<table style='width:100%; border-collapse: collapse; margin-top:10px;'>");
        if (depositFee != 0) {
            html.append("<tr>")
                    .append("<td style='padding:8px; border-bottom:1px solid #eee;'>Deposit Fee</td>")
                    .append("<td style='padding:8px; text-align:right; border-bottom:1px solid #eee;'>")
                    .append(String.format("$%.2f", depositFee))
                    .append("</td></tr>");
        }
        if (rentalFee != 0) {
            html.append("<tr>")
                    .append("<td style='padding:8px; border-bottom:1px solid #eee;'>Rental Fee</td>")
                    .append("<td style='padding:8px; text-align:right; border-bottom:1px solid #eee;'>")
                    .append(String.format("$%.2f", rentalFee))
                    .append("</td></tr>");
        }
        if (penaltyFee != 0) {
            html.append("<tr>")
                    .append("<td style='padding:8px; border-bottom:1px solid #eee;'>Penalty Fee</td>")
                    .append("<td style='padding:8px; text-align:right; border-bottom:1px solid #eee;'>")
                    .append(String.format("$%.2f", penaltyFee))
                    .append("</td></tr>");
        }
        if (total != 0) {
            html.append("<tr>")
                    .append("<td style='padding:8px; font-weight:bold;'>Total</td>")
                    .append("<td style='padding:8px; text-align:right; font-weight:bold;'>")
                    .append(String.format("$%.2f", total))
                    .append("</td></tr>");
        }
        html.append("</table>");
        html.append("<p style='margin-top:20px;'>If you have any questions, feel free to reply to this email.</p>");
        html.append("<p style='color:gray; font-size:12px;'>Thank you for choosing our service 🚗💨</p>");
        html.append("</div></body></html>");

        return html.toString();
    }

}
