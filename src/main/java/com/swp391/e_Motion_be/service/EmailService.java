package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.email.PaymentEmailRequest;
import com.swp391.e_Motion_be.dto.email.PaymentItem;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.RentalCheckListRepository;
import com.swp391.e_Motion_be.util.CurrencyFee;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;
    private final JavaMailSender emailSender;
    private final RentalCheckListRepository rentalCheckListRepository;

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

    public void sendPaymentStatusToEmail(Payment payment, String url) {
        PaymentEmailRequest emailModel = createEmailContentModel(payment);

        Context context = new Context();
        context.setVariable("subject", emailModel.getSubject());
        context.setVariable("message", emailModel.getMessage());
        context.setVariable("paymentType", emailModel.getPaymentType());
        context.setVariable("paymentStatus", emailModel.getPaymentStatus());
        context.setVariable("statusColor", emailModel.getStatusColor());
        context.setVariable("items", emailModel.getItems());
        context.setVariable("vehicleDamages", emailModel.getVehicleDamages());
        context.setVariable("vehicleDamagesTotal", emailModel.getVehicleDamagesTotal());
        context.setVariable("total", emailModel.getTotal());
        context.setVariable("url", url);
        context.setVariable("totalDeposit", emailModel.getTotalDeposit());
        context.setVariable("refundAmount", emailModel.getRefundAmount());

        String htmlBody = templateEngine.process("payment-status-email", context);
        sendEmail(payment.getUser().getEmail(), emailModel.getSubject(), htmlBody);
    }

    // Tạo nội dung email dựa trên loại thanh toán
    private PaymentEmailRequest createEmailContentModel(Payment payment) {
        PaymentType type = payment.getType();

        return switch (type) {
            case RESERVATION -> createReservationEmail(payment);
            case RENTAL -> createRentalEmail(payment);
            case PENALTY_FEE_RENTAL -> createPenaltyFeeEmail(payment);
            case REFUND -> createRefundEmail(payment);
            default -> createDefaultEmail(payment);
        };
    }

    // Email cho thanh toán đặt cọc (Reservation)
    private PaymentEmailRequest createReservationEmail(Payment payment) {
        Deposit deposit = payment.getDeposit();
        List<PaymentItem> items = new ArrayList<>();
        double total = 0;

        if (deposit != null && deposit.getAmount() > 0) {
            items.add(PaymentItem.builder()
                    .label("Deposit Fee")
                    .amount(deposit.getAmount())
                    .build());
            total += deposit.getAmount();
        }

        return PaymentEmailRequest.builder()
                .subject("Reservation Confirmed - Payment Receipt")
                .message("Your reservation has been confirmed! Below are your payment details.")
                .paymentType(PaymentType.RESERVATION)
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(Collections.emptyMap())
                .total(total)
                .build();
    }

    // Email cho thanh toán thuê xe (Check-in)
    private PaymentEmailRequest createRentalEmail(Payment payment) {
        Rental rental = payment.getRental();
        Deposit deposit = payment.getDeposit();
        List<PaymentItem> items = new ArrayList<>();
        double total = 0;

        if (deposit != null && deposit.getAmount() > 0) {
            items.add(PaymentItem.builder()
                    .label("Deposit Fee")
                    .amount(deposit.getAmount())
                    .build());
            total += deposit.getAmount();
        }

        if (rental != null && rental.getRentFee() > 0) {
            items.add(PaymentItem.builder()
                    .label("Rental Fee")
                    .amount(rental.getRentFee())
                    .build());
            total += rental.getRentFee();
        }

        return PaymentEmailRequest.builder()
                .subject("Rental Started - Payment Receipt")
                .message("Your rental has started! Below are your payment details.")
                .paymentType(PaymentType.RENTAL)
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(Collections.emptyMap())
                .total(total)
                .build();
    }

    // Email cho thanh toán phí phạt (Check-out)
    private PaymentEmailRequest createPenaltyFeeEmail(Payment payment) {
        Rental rental = payment.getRental();
        List<PaymentItem> items = new ArrayList<>();
        double total = 0;
        double damageTotal = 0;

        // Lấy rental checklist (check-out)
        RentalCheckList checkOut = rental.getRentalCheckLists().stream()
                .filter(r -> r.getType() == CheckType.CHECK_OUT)
                .findFirst()
                .orElse(null);

        // Lấy vehicle log
        VehicleLog vehicleLog = rental.getVehicleLog();

        // Thêm phí trả xe trễ
        if (checkOut != null && checkOut.getFee() > 0) {
            items.add(PaymentItem.builder()
                    .label("Late Return And Pin Penalty")
                    .amount(checkOut.getFee())
                    .build());
            total += checkOut.getFee();
        }

        // Thêm các damage charges (sẽ hiển thị chi tiết ở bảng riêng)
        Map<String, Double> vehicleDamages = Collections.emptyMap();
        if (vehicleLog != null && vehicleLog.getRepairCost() != null && !vehicleLog.getRepairCost().isEmpty()) {
            vehicleDamages = vehicleLog.getRepairCost();
            damageTotal = vehicleDamages.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            total += damageTotal;
        }

        return PaymentEmailRequest.builder()
                .subject("Rental Completed - Final Payment Receipt")
                .message("Your rental has been completed. Below is your final payment summary.")
                .paymentType(PaymentType.PENALTY_FEE_RENTAL)
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(vehicleDamages)
                .vehicleDamagesTotal(damageTotal)
                .total(total)
                .build();
    }

    // Email mặc định cho các loại thanh toán khác
    private PaymentEmailRequest createDefaultEmail(Payment payment) {
        return PaymentEmailRequest.builder()
                .subject("Payment Confirmation")
                .message("Thank you for your payment. Below are your transaction details.")
                .paymentType(payment.getType())
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(Collections.emptyList())
                .vehicleDamages(Collections.emptyMap())
                .total(0)
                .build();
    }

    // Email cho hoàn tiền (Refund)
    private PaymentEmailRequest createRefundEmail(Payment payment) {
        Rental rental = payment.getRental();

        if (rental == null) {
            return PaymentEmailRequest.builder()
                    .subject("Refund Processed - Payment Receipt")
                    .message("Your refund has been processed!")
                    .paymentType(PaymentType.REFUND)
                    .paymentStatus(payment.getStatus().toString())
                    .statusColor(getStatusColor(payment.getStatus()))
                    .items(Collections.emptyList())
                    .vehicleDamages(Collections.emptyMap())
                    .vehicleDamagesTotal(0)
                    .total(0)
                    .totalDeposit(payment.getDeposit() != null ? payment.getDeposit().getAmount() : 0)
                    .refundAmount(payment.getAmount())
                    .build();
        }

        double rentalDeposit = rental.getDeposit() != null ? rental.getDeposit().getAmount() : 0;
        double reservationDeposit = payment.getDeposit() != null ? payment.getDeposit().getAmount() : 0;
        double totalDeposit = rentalDeposit + reservationDeposit;
        List<PaymentItem> items = new ArrayList<>();
        double totalDeductions = 0;
        double damageTotal = 0;

        // Get rental checklist (check-out)
        RentalCheckList checkOut = null;
        if (rental.getRentalCheckLists() != null) {
            checkOut = rental.getRentalCheckLists().stream()
                    .filter(r -> r.getType() == CheckType.CHECK_OUT)
                    .findFirst()
                    .orElse(null);
        }

        // Get vehicle log
        VehicleLog vehicleLog = rental.getVehicleLog();

        // Add late return fee
        if (checkOut != null && checkOut.getFee() != null && checkOut.getFee() > 0) {
            items.add(PaymentItem.builder()
                    .label("Late Return And Pin Penalty")
                    .amount(checkOut.getFee())
                    .build());
            totalDeductions += checkOut.getFee();
        }

        // Add damage charges
        Map<String, Double> vehicleDamages = Collections.emptyMap();
        if (vehicleLog != null && vehicleLog.getRepairCost() != null && !vehicleLog.getRepairCost().isEmpty()) {
            vehicleDamages = vehicleLog.getRepairCost();
            damageTotal = vehicleDamages.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            totalDeductions += damageTotal;
        }

        // Calculate actual refund amount
        double actualRefundAmount = totalDeposit - totalDeductions;

        // Validation: refund shouldn't be negative
        if (actualRefundAmount < 0) {
            actualRefundAmount = 0;
        }

        return PaymentEmailRequest.builder()
                .subject("Refund Processed - Payment Receipt")
                .message("Your refund has been processed! Below are your refund details.")
                .paymentType(PaymentType.REFUND)
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(vehicleDamages)
                .vehicleDamagesTotal(damageTotal)
                .total(totalDeductions)
                .totalDeposit(totalDeposit)
                .refundAmount(actualRefundAmount)
                .build();
    }

    // Lấy màu tương ứng với trạng thái thanh toán
    private String getStatusColor(PaymentStatus status) {
        return switch (status) {
            case SUCCESS -> "#38a169"; // Green
            case FAILED -> "#e53e3e";  // Red
            case PENDING -> "#ecc94b"; // Yellow
            case REFUND -> "#3182ce"; // Blue
            default -> "#718096";      // Gray
        };
    }

    public void sendRentalOverdueEmail(Rental rental) {
        String subject = "Your Rental is Overdue";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", rental.getUser().getFullName());
        context.setVariable("rentalId", rental.getId());
        context.setVariable("vehicleName", rental.getVehicle().getName());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-overdue-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendRentalExpiringEmail(Rental rental) {
        String subject = "Your Rental is About to Expire";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", rental.getUser().getFullName());
        context.setVariable("rentalId", rental.getId());
        context.setVariable("vehicleName", rental.getVehicle().getName());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-expiring-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendRentalReturnedNotification(Rental rental) {
        String subject = "Your vehicle has been successfully returned to the station.";

        RentalCheckList checkOut = rentalCheckListRepository.findByRental_Id(rental.getId()).stream()
                .filter(c -> c.getType() == CheckType.CHECK_OUT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKOUT_NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

        String startTimeFormatted = rental.getStartTime() != null
                ? rental.getStartTime().format(formatter)
                : "Not specified";
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";
        String actualReturnedTime = checkOut.getCreatedAt() != null
                ? checkOut.getCreatedAt().format(formatter)
                : "Not specified";
        Context context = new Context();
        context.setVariable("userFullName", rental.getUser().getFullName());
        context.setVariable("rentalId", rental.getId());
        context.setVariable("vehicleName", rental.getVehicle().getName());
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("startTime", startTimeFormatted);
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("actualVehicleReturned", actualReturnedTime);
        context.setVariable("location", rental.getStation().getAddress());
        context.setVariable("usageFee", CurrencyFee.toVND(checkOut.getFee()));
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-returned-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }


}
