package com.swp391.e_Motion_be.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.swp391.e_Motion_be.dto.email.PaymentEmailRequest;
import com.swp391.e_Motion_be.dto.email.PaymentItem;
import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.PaymentRepository;
import com.swp391.e_Motion_be.repository.RentalCheckListRepository;
import com.swp391.e_Motion_be.util.CurrencyFee;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;
    private final RentalCheckListRepository rentalCheckListRepository;
    private final PaymentRepository paymentRepository;

    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        Email from = new Email("030739230108@st.buh.edu.vn"); // verified sender
        Email toEmail = new Email(to);
        Content content = new Content("text/html", text);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);


            System.out.println("SendGrid Response Code: " + response.getStatusCode());
            System.out.println("SendGrid Response Body: " + response.getBody());
            System.out.println("SendGrid Response Headers: " + response.getHeaders());

            if (response.getStatusCode() >= 400) {
                System.err.println("SendGrid error: " + response.getStatusCode() + " " + response.getBody());
                throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendEmail(String to, String subject, String htmlMessage) {
        try {
            sendVerificationEmail(to, subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }
    public void sendVerificationEmail(User user) {
        String subject = "Xác minh tài khoản";
        String code = user.getVerificationCode() != null
                ? user.getVerificationCode()
                : user.getForgotPasswordCode();
        Context context = new Context();
        context.setVariable("code", code);
        String htmlMessage = templateEngine.process("verify-email", context);
        try{
            sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        }
        catch (MessagingException e){
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendReservationCodeEmail(Reservation reservation) {
        String subject = "Xác nhận đặt chỗ - Mã đặt xe " + reservation.getCode();
        String header = "Xác nhận đặt chỗ";
        String message = "Cảm ơn bạn đã đặt trước xe tại E-Motion. Thông tin chi tiết như sau:";

        String html = buildReservationHtml(reservation, header, message);

        try {
            sendEmail(reservation.getUser().getEmail(), subject, html);
        } catch (Exception e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    // format pickup time: e.g. "01 Oct 2025, 14:30"
    public String buildReservationHtml(Reservation reservation, String header, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String pickupTime = reservation.getStartTime() != null
                ? reservation.getStartTime().format(formatter)
                : "Not specified";

        Context ctx = new Context();
        ctx.setVariable("header", header);
        ctx.setVariable("message", message);
        ctx.setVariable("reservationCode", reservation.getCode());
        ctx.setVariable("pickupTime", pickupTime);
        ctx.setVariable("stationName", reservation.getStation().getName());

        return templateEngine.process("reservation-email", ctx);
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
        context.setVariable("penaltyTotal", emailModel.getPenaltyTotal());
        context.setVariable("rentalFee", emailModel.getRentalFee());
        context.setVariable("extraHourFee", emailModel.getExtraHourFee());
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
            case RENTAL_EXTENSION -> createRentalExtensionEmail(payment);
            default -> createDefaultEmail(payment);
        };
    }

    // Email cho thanh toán gia hạn thuê xe (Rental Extension)
    private PaymentEmailRequest createRentalExtensionEmail(Payment payment) {
        double extraHourFee = payment.getAmount();

        return PaymentEmailRequest.builder()
                .subject("Gia hạn thuê xe - Biên lai thanh toán")
                .message("Bạn đã gia hạn thời gian thuê xe. Chi tiết thanh toán nằm bên dưới.")
                .paymentType(PaymentType.RENTAL_EXTENSION)
                .paymentStatus(payment.getStatus().getDisplayName())
                .statusColor(getStatusColor(payment.getStatus()))
                .extraHourFee(extraHourFee)
                .items(List.of(
                ))
                .total(extraHourFee)
                .build();
    }


    // Email cho thanh toán đặt cọc (Reservation)
    private PaymentEmailRequest createReservationEmail(Payment payment) {
        Deposit deposit = payment.getDeposit();
        List<PaymentItem> items = new ArrayList<>();
        List<VehicleLogItem> vehicleLogItems = new ArrayList<>();
        double total = 0;

        if (deposit != null && deposit.getAmount() > 0) {
            items.add(PaymentItem.builder()
                    .label("Phí đặt cọc")
                    .amount(deposit.getAmount())
                    .build());
            total += deposit.getAmount();
        }

        return PaymentEmailRequest.builder()
                .subject("Xác nhận đặt chỗ - Biên lai thanh toán")
                .message("Đặt chỗ của bạn đã được xác nhận! Dưới đây là thông tin thanh toán của bạn.")
                .paymentType(PaymentType.RESERVATION)
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(vehicleLogItems)
                .total(total)
                .build();
    }

    // Email cho thanh toán thuê xe (Check-in)
    private PaymentEmailRequest createRentalEmail(Payment payment) {
        Rental rental = payment.getRental();
        Reservation reservation = rental.getReservation();
        Deposit deposit = payment.getDeposit();
        List<PaymentItem> items = new ArrayList<>();
        List<VehicleLogItem> vehicleLogItems = new ArrayList<>();
        double total = 0;

        if (deposit != null && deposit.getAmount() > 0) {
            items.add(PaymentItem.builder()
                    .label("Phí đặt cọc")
                    .amount(deposit.getAmount())
                    .build());
            total += deposit.getAmount();
        }

        if (rental != null && rental.getRentFee() > 0) {
            items.add(PaymentItem.builder()
                    .label("Phí thuê")
                    .amount(rental.getRentFee())
                    .build());
            total += rental.getRentFee();
        }

        if(reservation != null && reservation.getDeposit().getAmount() > 0) {
            items.add(PaymentItem.builder()
                    .label("Phí đặt cọc giữ chỗ")
                    .amount(reservation.getDeposit().getAmount())
                    .build());
        }

        return PaymentEmailRequest.builder()
                .subject("Bắt đầu cho thuê - Biên lai thanh toán")
                .message("Việc thuê xe của bạn đã bắt đầu! Dưới đây là thông tin thanh toán của bạn.")
                .paymentType(PaymentType.RENTAL)
                .paymentStatus(rental.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(vehicleLogItems)
                .total(total)
                .build();
    }

    // Email cho thanh toán phí phạt (Check-out)
    private PaymentEmailRequest createPenaltyFeeEmail(Payment payment) {
        Rental rental = payment.getRental();
        List<PaymentItem> items = new ArrayList<>();
        double penaltyTotal = 0;
        double damageTotal = 0;
        double totalDeposit = rental.getDeposit() != null ? rental.getDeposit().getAmount() : 0;
        totalDeposit += rental.getReservation() != null && rental.getReservation().getDeposit() != null
                ? rental.getReservation().getDeposit().getAmount() : 0;
        double rentalFee = rental.getRentFee();
        double total = 0;

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
                    .label("Trả trễ và phạt phí pin")
                    .amount(checkOut.getFee())
                    .build());
            penaltyTotal += checkOut.getFee();
        }

        // Thêm các damage charges (sẽ hiển thị chi tiết ở bảng riêng)
        List<VehicleLogItem> vehicleDamages = new ArrayList<>();
        if (vehicleLog != null && vehicleLog.getRepairItems() != null && !vehicleLog.getRepairItems().isEmpty()) {
             vehicleDamages = vehicleLog.getRepairItems();

            damageTotal = vehicleDamages
                        .stream()
                        .filter(Objects::nonNull)
                        .mapToDouble(VehicleLogItem::getCost)
                        .sum();

            penaltyTotal += damageTotal;
        }

        total = Math.abs(totalDeposit - penaltyTotal);
        Payment extraPayment = paymentRepository.findTopByDescriptionOrderByCreatedAtDesc("Thanh toán gia hạn cho ID thuê: "+rental.getId()).orElse(null);
        double extraHourFee = 0;
        if(extraPayment != null){
            extraHourFee = extraPayment.getAmount();
            if(rentalFee==0){
                total += extraPayment.getAmount();
            }
        }

        return PaymentEmailRequest.builder()
                .subject("Hoàn tất việc thuê - Biên lai thanh toán cuối cùng")
                .message("Đơn thuê xe của bạn đã hoàn tất. Dưới đây là bản tóm tắt thanh toán cuối cùng của bạn.")
                .paymentType(PaymentType.PENALTY_FEE_RENTAL)
                .paymentStatus(rental.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(vehicleDamages)
                .vehicleDamagesTotal(damageTotal)
                .totalDeposit(totalDeposit)
                .rentalFee(rentalFee)
                .extraHourFee(extraHourFee)
                .total(total)
                .penaltyTotal(penaltyTotal)
                .build();
    }

    // Email mặc định cho các loại thanh toán khác
    private PaymentEmailRequest createDefaultEmail(Payment payment) {
        List<VehicleLogItem> vehicleLogItems = new ArrayList<>();
        return PaymentEmailRequest.builder()
                .subject("Xác nhận thanh toán")
                .message("Cảm ơn bạn đã thanh toán. Dưới đây là thông tin chi tiết giao dịch của bạn.")
                .paymentType(payment.getType())
                .paymentStatus(payment.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(Collections.emptyList())
                .vehicleDamages(vehicleLogItems)
                .total(0)
                .build();
    }

    // Email cho hoàn tiền (Refund)
    private PaymentEmailRequest createRefundEmail(Payment payment) {
        Rental rental = payment.getRental();
        List<VehicleLogItem> vehicleLogItems = new ArrayList<>();
        if (rental == null) {
            return PaymentEmailRequest.builder()
                    .subject("Đã xử lý hoàn tiền - Biên lai thanh toán")
                    .message("Việc hoàn tiền của bạn đã được xử lý!")
                    .paymentType(PaymentType.REFUND)
                    .paymentStatus(payment.getStatus().toString())
                    .statusColor(getStatusColor(payment.getStatus()))
                    .items(Collections.emptyList())
                    .vehicleDamages(vehicleLogItems)
                    .vehicleDamagesTotal(0)
                    .total(0)
                    .totalDeposit(payment.getDeposit() != null ? payment.getDeposit().getAmount() : 0)
                    .refundAmount(payment.getAmount())
                    .build();
        }

        double totalDeposit = rental.getDeposit() != null ? rental.getDeposit().getAmount() : 0;
        totalDeposit += rental.getReservation() != null && rental.getReservation().getDeposit() != null
                ? rental.getReservation().getDeposit().getAmount() : 0;
        List<PaymentItem> items = new ArrayList<>();
        double penaltyTotal = 0;
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
                    .label("Trả trễ và phạt phí pin")
                    .amount(checkOut.getFee())
                    .build());
            penaltyTotal += checkOut.getFee();
        }

        // Add damage charges
        List<VehicleLogItem> vehicleDamages = new ArrayList<>();
        if (vehicleLog != null && vehicleLog.getRepairItems() != null && !vehicleLog.getRepairItems().isEmpty()) {
            vehicleDamages = vehicleLog.getRepairItems();

            damageTotal = vehicleDamages
                    .stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(VehicleLogItem::getCost)
                    .sum();
            penaltyTotal += damageTotal;
        }

        // Calculate actual refund amount
        double actualRefundAmount = totalDeposit - penaltyTotal;

        // Validation: refund shouldn't be negative
        if (actualRefundAmount < 0) {
            actualRefundAmount = 0;
        }

        return PaymentEmailRequest.builder()
                .subject("Đã xử lý hoàn tiền - Biên lai thanh toán")
                .message("Yêu cầu hoàn tiền của bạn đã được xử lý! Dưới đây là thông tin chi tiết về khoản hoàn tiền của bạn.")
                .paymentType(PaymentType.REFUND)
                .paymentStatus(rental.getStatus().toString())
                .statusColor(getStatusColor(payment.getStatus()))
                .items(items)
                .vehicleDamages(vehicleDamages)
                .vehicleDamagesTotal(damageTotal)
                .penaltyTotal(penaltyTotal)
                .totalDeposit(totalDeposit)
                .total(0)
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
        String subject = "Đơn thuê xe của bạn đã quá hạn.";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("userFullName", rental.getUser().getFullName());
        context.setVariable("rentalId", rental.getId());
        context.setVariable("vehicleName", rental.getVehicle().getName());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("stationAddress", rental.getStation().getAddress());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-overdue-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendRentalExpiringEmail(Rental rental) {
        String subject = "Đơn thuê xe của bạn sắp hết hạn";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", rental.getUser().getFullName());
        context.setVariable("rentalId", rental.getId());
        context.setVariable("vehicleName", rental.getVehicle().getName());
        context.setVariable("startTime", rental.getStartTime());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("stationAddress", rental.getStation().getAddress());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-expiring-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendRentalReturnedNotification(Rental rental) {
        String subject = "Xe của bạn đã được trả về trạm thành công.";

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
        context.setVariable("location", rental.getStation().getName());
        context.setVariable("stationAddress", rental.getStation().getAddress());
        context.setVariable("usageFee", CurrencyFee.toVND(checkOut.getFee()));
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-returned-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendReservationOverdueEmail(Reservation reservation) {
        String subject = "Đơn đặt trước của bạn đã quá hạn.";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = reservation.getEndTime() != null
                ? reservation.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", reservation.getUser().getFullName());
        context.setVariable("reservationCode", reservation.getCode());
        context.setVariable("vehicleName", reservation.getVehicle().getName());
        context.setVariable("startTime", reservation.getStartTime());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", reservation.getStation().getName());
        context.setVariable("stationAddress", reservation.getStation().getAddress());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("reservation-overdue-email", context);

        try {
            sendVerificationEmail(reservation.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendReservationExpiringEmail(Reservation reservation) {
        String subject = "Đơn đặt trước của bạn sắp hết hạn";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = reservation.getEndTime() != null
                ? reservation.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", reservation.getUser().getFullName());
        context.setVariable("reservationCode", reservation.getCode());
        context.setVariable("vehicleName", reservation.getVehicle().getName());
        context.setVariable("startTime", reservation.getStartTime());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", reservation.getStation().getName());
        context.setVariable("stationAddress", reservation.getStation().getAddress());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("reservation-expiring-email", context);

        try {
            sendVerificationEmail(reservation.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendReservationCancelEmail(Reservation reservation) {
        String subject = "Đơn đặt trước của bạn đã huỷ thành công";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = reservation.getEndTime() != null
                ? reservation.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", reservation.getUser().getFullName());
        context.setVariable("reservationCode", reservation.getCode());
        context.setVariable("vehicleName", reservation.getVehicle().getName());
        context.setVariable("startTime", reservation.getStartTime());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", reservation.getStation().getName());
        context.setVariable("stationAddress", reservation.getStation().getAddress());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("reservation-cancel-email", context);

        try {
            sendVerificationEmail(reservation.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendRentalCancelEmail(Rental rental) {
        String subject = "Đơn thuê của bạn đã huỷ thành công";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";


        Context context = new Context();
        context.setVariable("userFullName", rental.getUser().getFullName());
        context.setVariable("rentalId", rental.getId());
        context.setVariable("vehicleName", rental.getVehicle().getName());
        context.setVariable("startTime", rental.getStartTime());
        context.setVariable("endTime", endTimeFormatted);
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("stationAddress", rental.getStation().getAddress());
        context.setVariable("contactLink", "https://e-motion.vn/support");

        String htmlMessage = templateEngine.process("rental-cancel-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    public void sendContractEmail(Rental rental, String contractUrl) {
        String subject = "Hợp đồng thuê xe của bạn";

        Context context = new Context();
        context.setVariable("renterName", rental.getUser().getFullName());
        context.setVariable("contractUrl", contractUrl);
        context.setVariable("carName", rental.getVehicle().getName());
        context.setVariable("carBrand", rental.getVehicle().getBrand());
        context.setVariable("carCategory", rental.getVehicle().getCategory());
        context.setVariable("plateNumber", rental.getVehicle().getPlateNumber());
        context.setVariable("rentFee", rental.getRentFee());
        context.setVariable("startTime", rental.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("endTime", rental.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("stationName", rental.getStation().getName());
        context.setVariable("stationAddress", rental.getStation().getAddress());
        context.setVariable("payDate", rental.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String htmlContent = templateEngine.process("contract-email", context);

        try {
            sendVerificationEmail(rental.getUser().getEmail(), subject, htmlContent);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }
}
