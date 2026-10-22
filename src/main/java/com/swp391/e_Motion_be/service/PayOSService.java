package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.config.PayOSConfig;
import com.swp391.e_Motion_be.dto.requests.payment.CompensationPaymentRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PayOSWebhookRequest;
import com.swp391.e_Motion_be.dto.responses.PayOSResponse;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.repository.*;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSService {

    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final ReservationRepository reservationRepository;
    private final DepositRepository depositRepository;
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
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

        if (payOS == null || !payOSConfig.isConfigured()) {
            log.error("PayOS is not configured with valid credentials in application.properties or .env");
            throw new AppException(ErrorCode.CREATE_PAYMENT_URL_FAILED);
        }

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
            log.info("Created PayOS official payment link for reservation #{}: orderCode={}, url={}, account={}, name={}",
                    resCode, orderCode, checkoutData.getCheckoutUrl(), checkoutData.getAccountNumber(), checkoutData.getAccountName());

            saveOrUpdatePaymentRecord(deposit, amount, orderCode, PaymentMethod.PAYOS);

            // Tạo URL ảnh VietQR tương ứng từ chính tài khoản thật của PayOS
            String officialQrImageUrl = "https://img.vietqr.io/image/" + checkoutData.getBin() + "-"
                    + checkoutData.getAccountNumber() + "-compact2.png?amount=" + (long) amount
                    + "&addInfo=" + description + "&accountName="
                    + URLEncoder.encode(checkoutData.getAccountName(), StandardCharsets.UTF_8);

            String bankName = resolveBankName(checkoutData.getBin());

            return PayOSResponse.builder()
                    .checkoutUrl(checkoutData.getCheckoutUrl())
                    .qrCode(officialQrImageUrl)
                    .accountNumber(checkoutData.getAccountNumber())
                    .accountName(checkoutData.getAccountName())
                    .bin(checkoutData.getBin())
                    .bankName(bankName)
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .status("PENDING")
                    .build();
        } catch (Exception e) {
            log.error("PayOS SDK error creating payment link for reservation #{}: {}", resCode, e.getMessage(), e);
            throw new AppException(ErrorCode.CREATE_PAYMENT_URL_FAILED);
        }
    }

    /**
     * Tạo thông tin thanh toán VietQR / PayOS cho chi phí đền bù hư hại hoặc phụ phí khi khách trả xe
     */
    @Transactional
    public PayOSResponse createRentalCompensationPaymentLink(Long rentalId, CompensationPaymentRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        double amount;
        if (request != null && request.getAmount() != null && request.getAmount() > 0) {
            amount = request.getAmount();
        } else {
            RentalCheckList rentalCheckList = rental.getRentalCheckLists() != null
                    ? rental.getRentalCheckLists().stream()
                            .filter(r -> r.getType() != null && "CHECK_OUT".equalsIgnoreCase(r.getType().toString()))
                            .findFirst()
                            .orElse(null)
                    : null;

            double checkListFee = rentalCheckList != null ? rentalCheckList.getFee() : 0.0;
            double reservationDepositAmount = (rental.getReservation() != null && rental.getReservation().getDeposit() != null)
                    ? rental.getReservation().getDeposit().getAmount() : 0.0;
            double rentalDepositAmount = rental.getDeposit() != null ? rental.getDeposit().getAmount() : 0.0;

            VehicleLog vehicleLog = rental.getVehicleLog();
            double vehicleDamageFee = vehicleLog != null ? vehicleLog.getCost() : 0.0;

            double totalCharges = vehicleDamageFee + checkListFee;
            double totalDeposits = reservationDepositAmount + rentalDepositAmount;
            amount = totalCharges - totalDeposits;
        }

        if (amount <= 0) {
            log.warn("Rental #{} does not require compensation payment. Calculated amount: {}", rentalId, amount);
            throw new AppException(ErrorCode.NO_COMPENSATION_FEE_REQUIRED);
        }

        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(4) + (rentalId % 1000));
        String description = "DEN BU DON " + rentalId;
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        if (payOS == null || !payOSConfig.isConfigured()) {
            log.error("PayOS is not configured with valid credentials in application.properties or .env");
            throw new AppException(ErrorCode.CREATE_PAYMENT_URL_FAILED);
        }

        try {
            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name("DEN BU DON " + rentalId)
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
            log.info("Created PayOS compensation payment link for rental #{}: orderCode={}, url={}, account={}, name={}",
                    rentalId, orderCode, checkoutData.getCheckoutUrl(), checkoutData.getAccountNumber(), checkoutData.getAccountName());

            saveOrUpdateRentalPaymentRecord(rental, amount, orderCode, description, PaymentMethod.PAYOS);

            // Tạo URL ảnh VietQR tương ứng từ chính tài khoản thật của PayOS
            String officialQrImageUrl = "https://img.vietqr.io/image/" + checkoutData.getBin() + "-"
                    + checkoutData.getAccountNumber() + "-compact2.png?amount=" + (long) amount
                    + "&addInfo=" + URLEncoder.encode(description, StandardCharsets.UTF_8) + "&accountName="
                    + URLEncoder.encode(checkoutData.getAccountName(), StandardCharsets.UTF_8);

            String bankName = resolveBankName(checkoutData.getBin());

            return PayOSResponse.builder()
                    .checkoutUrl(checkoutData.getCheckoutUrl())
                    .qrCode(officialQrImageUrl)
                    .accountNumber(checkoutData.getAccountNumber())
                    .accountName(checkoutData.getAccountName())
                    .bin(checkoutData.getBin())
                    .bankName(bankName)
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .status("PENDING")
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("PayOS SDK error creating compensation payment link for rental #{}: {}", rentalId, e.getMessage(), e);
            throw new AppException(ErrorCode.CREATE_PAYMENT_URL_FAILED);
        }
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
        String ref = data.getReference() != null ? data.getReference() : "PAYOS_WH_" + System.currentTimeMillis();

        // 1. Kiểm tra theo orderCode xem thanh toán thuộc về đơn thuê xe (Rental) hay giữ chỗ (Reservation)
        if (data.getOrderCode() != null) {
            Optional<Payment> paymentOpt = paymentRepository.findByTxnRef(data.getOrderCode().toString());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (payment.getType() == PaymentType.PENALTY_FEE_RENTAL && payment.getRental() != null) {
                    log.info("PayOS webhook matched Rental penalty payment #{} for rental #{}", payment.getId(), payment.getRental().getId());
                    return confirmRentalCompensation(payment, ref);
                }
                if (payment.getDeposit() != null && payment.getDeposit().getReservation() != null) {
                    return confirmReservation(payment.getDeposit().getReservation(), ref);
                }
            }
        }

        // 2. Kiểm tra theo nội dung chuyển khoản "DEN BU DON {rentalId}"
        if (description.toUpperCase().contains("DEN BU DON")) {
            String rentalIdStr = description.replaceAll("[^0-9]", "").trim();
            if (!rentalIdStr.isEmpty()) {
                try {
                    Long rentalId = Long.parseLong(rentalIdStr);
                    Payment payment = paymentRepository.findTopByTypeAndRentalIdOrderByCreatedAtDesc(PaymentType.PENALTY_FEE_RENTAL, rentalId)
                            .orElse(null);
                    if (payment != null) {
                        log.info("PayOS webhook matched description for rental penalty #{} for rental #{}", payment.getId(), rentalId);
                        return confirmRentalCompensation(payment, ref);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse rentalId from description: {}", description);
                }
            }
        }

        // 3. Fallback xử lý cho Reservation
        String code = description.replaceFirst("(?i)^BBC", "").trim();
        Reservation reservation = null;
        if (!code.isEmpty()) {
            reservation = reservationRepository.findByCode(code).orElse(null);
        }
        if (reservation == null && data.getOrderCode() != null) {
            reservation = reservationRepository.findByCode(data.getOrderCode().toString()).orElse(null);
        }
        if (reservation == null) {
            log.warn("Reservation or Rental not found for PayOS webhook description: {}", description);
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return confirmReservation(reservation, ref);
    }

    /**
     * Kênh kiểm tra chủ động (Active Check) khi người dùng bấm "Xác nhận đã thanh toán"
     */
    @Transactional
    public PaymentResponse checkAndConfirmPayment(String code) {
        String cleanCode = code.replaceFirst("(?i)^BBC", "").trim();

        // Kiểm tra xem code có tương ứng với Payment hoặc Rental ID không
        Optional<Reservation> reservationOpt = reservationRepository.findByCode(cleanCode);
        if (reservationOpt.isEmpty()) {
            Optional<Payment> paymentOpt = paymentRepository.findByTxnRef(cleanCode);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (payment.getType() == PaymentType.PENALTY_FEE_RENTAL && payment.getRental() != null) {
                    return checkAndConfirmRentalPayment(payment.getRental().getId());
                }
            }
            try {
                Long rentalId = Long.parseLong(cleanCode);
                Optional<Rental> rentalOpt = rentalRepository.findById(rentalId);
                if (rentalOpt.isPresent()) {
                    return checkAndConfirmRentalPayment(rentalId);
                }
            } catch (NumberFormatException ignored) {}
        }

        Reservation reservation = reservationOpt
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        // Nếu đơn đã xác nhận rồi thì trả về thông tin hiện tại
        if (reservation.getStatus() == ReservationStatus.CONFIRM) {
            Payment payment = getLatestPayment(reservation);
            if (payment != null) {
                return paymentMapper.toPaymentResponse(payment);
            }
        }

        // Bắt buộc phải đối soát thành công trạng thái PAID qua PayOS API
        if (payOS != null && payOSConfig.isConfigured()) {
            try {
                Payment latestPayment = getLatestPayment(reservation);
                if (latestPayment != null && latestPayment.getTxnRef() != null) {
                    long orderCode = Long.parseLong(latestPayment.getTxnRef());
                    PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
                    if (paymentLink != null && paymentLink.getStatus() == PaymentLinkStatus.PAID) {
                        log.info("PayOS confirmed payment status is PAID for orderCode: {}", orderCode);
                        return confirmReservation(reservation, "PAYOS_VERIFIED_" + orderCode);
                    } else {
                        log.warn("PayOS payment for orderCode {} is not PAID (current status: {})",
                                orderCode, paymentLink != null ? paymentLink.getStatus() : "null");
                        throw new AppException(ErrorCode.PAYMENT_NOT_RECEIVED);
                    }
                }
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                log.error("PayOS inquiry error for reservation {}: {}", reservation.getCode(), e.getMessage());
                throw new AppException(ErrorCode.PAYMENT_NOT_RECEIVED);
            }
        }

        throw new AppException(ErrorCode.PAYMENT_NOT_RECEIVED);
    }

    /**
     * Kênh kiểm tra và đối soát thanh toán đền bù theo Rental ID
     */
    @Transactional
    public PaymentResponse checkAndConfirmRentalPayment(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        Payment payment = paymentRepository.findTopByTypeAndRentalIdOrderByCreatedAtDesc(PaymentType.PENALTY_FEE_RENTAL, rental.getId())
                .orElse(null);

        if (payment == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return paymentMapper.toPaymentResponse(payment);
        }

        if (payOS != null && payOSConfig.isConfigured() && payment.getTxnRef() != null) {
            try {
                long orderCode = Long.parseLong(payment.getTxnRef());
                PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
                if (paymentLink != null && paymentLink.getStatus() == PaymentLinkStatus.PAID) {
                    log.info("PayOS confirmed rental payment status is PAID for orderCode: {}", orderCode);
                    return confirmRentalCompensation(payment, "PAYOS_VERIFIED_" + orderCode);
                } else {
                    log.warn("PayOS rental payment for orderCode {} is not PAID (current status: {})",
                            orderCode, paymentLink != null ? paymentLink.getStatus() : "null");
                    throw new AppException(ErrorCode.PAYMENT_NOT_RECEIVED);
                }
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                log.error("PayOS inquiry error for rental #{}: {}", rental.getId(), e.getMessage());
                throw new AppException(ErrorCode.PAYMENT_NOT_RECEIVED);
            }
        }

        throw new AppException(ErrorCode.PAYMENT_NOT_RECEIVED);
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
        Payment latest = paymentRepository.findTopByTypeAndDepositIdOrderByCreatedAtDesc(PaymentType.RESERVATION, deposit.getId())
                .orElse(null);
        if (latest != null && latest.getStatus() == PaymentStatus.PENDING) {
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
        long epochSeconds = System.currentTimeMillis() / 1000L;
        return (epochSeconds % 100000000L) * 100L + (reservationId % 100L);
    }

    private PaymentResponse confirmRentalCompensation(Payment payment, String transactionNo) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setMethod(PaymentMethod.PAYOS);
        payment.setBankCode("MBBANK");
        payment.setTransactionNo(transactionNo);
        payment.setPayDate(LocalDateTime.now());
        paymentRepository.save(payment);

        Rental rental = payment.getRental();
        if (rental != null) {
            // Cộng điểm cho user nếu có
            if (rental.getStartTime() != null && rental.getEndTime() != null && rental.getVehicle() != null) {
                long hours = Duration.between(rental.getStartTime(), rental.getEndTime()).toHours();
                int pointPerHour = rental.getVehicle().getPoint();
                int earnedPoints = (int) (hours * pointPerHour);

                User user = rental.getUser();
                if (user != null) {
                    user.setPoint(user.getPoint() + earnedPoints);
                    userRepository.save(user);
                }
            }

            rental.setStatus(RentalStatus.COMPLETED);
            if (rental.getVehicleLog() == null && rental.getVehicle() != null) {
                rental.getVehicle().setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(rental.getVehicle());
            }
            rentalRepository.save(rental);
            log.info("Rental #{} marked as COMPLETED via PayOS successfully.", rental.getId());

            try {
                emailService.sendPaymentStatusToEmail(payment, null);
            } catch (Exception e) {
                log.error("Error sending rental payment email: {}", e.getMessage());
            }
        }

        return paymentMapper.toPaymentResponse(payment);
    }

    private void saveOrUpdateRentalPaymentRecord(Rental rental, double amount, long orderCode, String description, PaymentMethod method) {
        Payment latest = paymentRepository.findTopByTypeAndRentalIdOrderByCreatedAtDesc(PaymentType.PENALTY_FEE_RENTAL, rental.getId())
                .orElse(null);
        if (latest != null && latest.getStatus() == PaymentStatus.PENDING) {
            latest.setMethod(method);
            latest.setAmount(amount);
            latest.setTxnRef(String.valueOf(orderCode));
            latest.setDescription(description);
            paymentRepository.save(latest);
            return;
        }

        Payment payment = Payment.builder()
                .rental(rental)
                .user(rental.getUser())
                .amount(amount)
                .description(description)
                .status(PaymentStatus.PENDING)
                .type(PaymentType.PENALTY_FEE_RENTAL)
                .method(method)
                .txnRef(String.valueOf(orderCode))
                .build();
        paymentRepository.save(payment);
    }

    private String resolveBankName(String bin) {
        if (bin == null) return "MBBank";
        return switch (bin.trim()) {
            case "970422" -> "MBBank";
            case "970415" -> "VietinBank";
            case "970436" -> "Vietcombank";
            case "970418" -> "BIDV";
            case "970407" -> "Techcombank";
            case "970423" -> "TPBank";
            case "970432" -> "VPBank";
            case "970405" -> "Agribank";
            case "970416" -> "ACB";
            default -> "MBBank";
        };
    }
}
