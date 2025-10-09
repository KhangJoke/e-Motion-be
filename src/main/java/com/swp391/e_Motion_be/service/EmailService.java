package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.email.PaymentEmailRequest;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;
    private final JavaMailSender emailSender;

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
        // 1. Dùng một hàm helper để chuẩn bị toàn bộ dữ liệu
        PaymentEmailRequest emailModel = createEmailContentModel(payment);

        // 2. Dùng Thymeleaf để tạo HTML
        Context context = new Context();
        context.setVariable("subject", emailModel.getSubject());
        context.setVariable("message", emailModel.getMessage());
        context.setVariable("paymentStatus", emailModel.getPaymentStatus());
        context.setVariable("statusColor", emailModel.getStatusColor());
        context.setVariable("depositFee", emailModel.getDepositFee());
        context.setVariable("rentalFee", emailModel.getRentalFee());
        context.setVariable("penaltyFee", emailModel.getPenaltyFee());
        context.setVariable("vehicleLogFee", emailModel.getVehicleLogFee());
        context.setVariable("vehicleDamages", emailModel.getVehicleDamages());
        context.setVariable("total", emailModel.getTotal());

        String htmlBody = templateEngine.process("payment-email", context);

        // 3. Gửi email
        sendEmail(payment.getUser().getEmail(), emailModel.getSubject(), htmlBody);
    }

    // Tạo model dữ liệu cho email
    private PaymentEmailRequest createEmailContentModel(Payment payment) {
        // Khởi tạo các biến
        Rental rental = null;
        Deposit deposit = null;
        RentalCheckList rentalCheckList = null;
        VehicleLog vehicleLog = null;

        // Logic if-else if để lấy dữ liệu từ Payment object
        if (payment.getType() == PaymentType.RENTAL) {
            rental = payment.getRental();
            deposit = payment.getDeposit();
        } else if (payment.getType() == PaymentType.RESERVATION) {
            deposit = payment.getDeposit();
        } else if (payment.getType() == PaymentType.PENALTY_FEE_RENTAL) {
            rental = payment.getRental();
            rentalCheckList = rental.getRentalCheckLists().stream()
                    .filter(r -> r.getType().toString().equalsIgnoreCase(CheckType.CHECK_OUT.toString()))
                    .findFirst().orElse(null);
            vehicleLog = rental.getVehicleLog();
            deposit = rental.getDeposit();
        }

        // Tính toán các chi phí
        double total = 0;
        double depositFee = 0;
        double rentalFee = 0;
        double penaltyFee = 0;
        double vehicleLogFee = 0;

        if (deposit != null && deposit.getAmount() > 0) {
            depositFee = deposit.getAmount();
            total += deposit.getAmount();
        }
        if (rental != null && rental.getRentFee() > 0) {
            rentalFee = rental.getRentFee();
            total += rental.getRentFee();
        }
        if (rentalCheckList != null && rentalCheckList.getFee() > 0) {
            penaltyFee = rentalCheckList.getFee();
            total += rentalCheckList.getFee();
        }
        if (vehicleLog != null && vehicleLog.getCost() > 0) {
            vehicleLogFee = vehicleLog.getCost();
            total += vehicleLog.getCost();
        }

        // Lấy trạng thái và màu sắc
        String paymentStatus = payment.getStatus().toString();
        String statusColor = switch (paymentStatus.toUpperCase()) {
            case "SUCCESS" -> "#38a169";
            case "FAILED" -> "#e53e3e";
            default -> "#718096";
        };

        // Trả về model hoàn chỉnh
        return PaymentEmailRequest.builder()
                .subject("Your Payment Confirmation")
                .message("Thanks for your payment! Below are your transaction details.")
                .paymentStatus(paymentStatus)
                .statusColor(statusColor)
                .depositFee(depositFee)
                .rentalFee(rentalFee)
                .penaltyFee(penaltyFee)
                .vehicleLogFee(vehicleLogFee)
                .vehicleDamages(vehicleLog != null ? vehicleLog.getRepairCost() : Collections.emptyMap())
                .total(total)
                .build();
    }

}
