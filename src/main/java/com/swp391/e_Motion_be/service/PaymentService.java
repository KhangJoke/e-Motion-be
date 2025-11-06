package com.swp391.e_Motion_be.service;

import com.google.gson.Gson;
import com.swp391.e_Motion_be.config.VNPayConfig;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PaymentRequest;
import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.requests.payment.UpdatePaymentRequest;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.TransactionResponse;
import com.swp391.e_Motion_be.dto.responses.VnpayResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.repository.*;
import com.swp391.e_Motion_be.util.QRCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final DepositRepository depositRepository;
    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;
    private final VNPayConfig vnPayConfig;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;

    @Transactional
    public VnpayResponse createPaymentUrl(CreatePaymentUrlRequest request, String ipAddr) throws Exception {
        log.info("Creating payment URL for user: {}", request.getUserEmail());

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = vnPayConfig.generateTxnRef();

        // Fetch and validate entities
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Rental rental = null;
        if (request.getRentalId() != null) {
            rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
            if(rental.getStatus() != RentalStatus.PENDING && rental.getStatus() != RentalStatus.PENDING_FEE){
                throw new AppException(ErrorCode.RENTAL_CANNOT_BE_PAID);
            }
        }

        Deposit deposit = null;
        if (request.getDepositId() != null) {
            deposit = depositRepository.findById(request.getDepositId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            if(deposit.getStatus() != DepositStatus.PENDING && deposit.getStatus() != DepositStatus.FAILED){
                throw new AppException(ErrorCode.DEPOSIT_CANNOT_BE_PAID);
            }
        }

        // Create payment record
        Payment payment = Payment.builder()
                .user(user)
                .rental(rental)
                .deposit(deposit)
                .txnRef(vnp_TxnRef)
                .amount(request.getAmount())
                .description(request.getDescription())
                .method(PaymentMethod.VNPAY)
                .type(request.getType())
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Build VNPay parameters
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf((long) (Double.parseDouble(String.format("%.2f", request.getAmount())) * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", request.getDescription());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddr);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = payment.getCreatedAt().format(formatter);
        String expireDate = LocalDateTime.now().plusMinutes(15).format(formatter);

        vnp_Params.put("vnp_CreateDate", createDate);
        vnp_Params.put("vnp_ExpireDate", expireDate);

        // Build query string with sorted parameters
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        int count = 0; // dùng để kiểm tra phần tử cuối cùng
        int size = fieldNames.size();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                count++;
                if (count < size) { // thêm & nếu chưa phải phần tử cuối
                    hashData.append('&');
                    query.append('&');
                }
            }
        }


        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + query;
        log.info("Payment URL created successfully for txnRef: {}", vnp_TxnRef);

        return new VnpayResponse(paymentUrl, QRCode.generateVnpayQR(paymentUrl));
    }

    @Transactional
    public PaymentResponse handleReturn(Map<String, String> params) {
        String vnp_TxnRef = params.get("vnp_TxnRef");
        log.info("Handling payment return for txnRef: {}", vnp_TxnRef);

        String vnp_SecureHash = params.get("vnp_SecureHash");

        // Validate signature
        if (!vnPayConfig.validateSignature(params, vnp_SecureHash)) {
            log.error("Invalid signature for txnRef: {}", vnp_TxnRef);
            throw new AppException(ErrorCode.VNPAY_KEY_INVALID);
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");
        String payDate = params.get("vnp_PayDate");

        Payment payment = paymentRepository.findByTxnRef(vnp_TxnRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

        if("94".equals(responseCode)){
            processFailedPayment(payment);
            log.warn("Payment failed for txnRef: {} with code: {}", vnp_TxnRef, responseCode);
            emailService.sendPaymentStatusToEmail(payment, null);
            payment.setTxnRef(vnPayConfig.generateTxnRef());
            paymentRepository.save(payment);
            return null;
        }

        // Prevent duplicate processing
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment already processed: {}", vnp_TxnRef);
            return paymentMapper.toPaymentResponse(payment);
        }

        // Update payment details
        payment.setResponseCode(responseCode);
        payment.setTransactionNo(transactionNo);
        payment.setBankCode(bankCode);

        if (payDate != null && !payDate.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                payment.setPayDate(LocalDateTime.parse(payDate, formatter));
            } catch (Exception e) {
                log.error("Error parsing payDate: {}", payDate, e);
            }
        }

        // Process payment result
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            processSuccessfulPayment(payment);
            log.info("Payment successful for txnRef: {}", vnp_TxnRef);
            paymentRepository.save(payment);
            emailService.sendPaymentStatusToEmail(payment, null);
        } else {
            processFailedPayment(payment);
            log.warn("Payment failed for txnRef: {} with code: {}", vnp_TxnRef, responseCode);
            emailService.sendPaymentStatusToEmail(payment, null);
        }

        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional
    protected void processSuccessfulPayment(Payment payment) {
        PaymentType type = payment.getType();

        switch (type) {
            case RESERVATION:
                handleSuccessfulReservation(payment);
                break;

            case RENTAL:
                handleSuccessfulRental(payment);
                break;

            case PENALTY_FEE_RENTAL:
                handleSuccessfulPenaltyFee(payment);
                break;

            case RENTAL_EXTENSION:
                handleSuccessfulExtension(payment);
                break;

            default:
                log.warn("No associated action for payment type: {}", type);
        }
    }

    @Transactional
    protected void processFailedPayment(Payment payment) {
        PaymentType type = payment.getType();

        switch (type) {
            case RENTAL_EXTENSION:
                handleFailedExtension(payment);
                break;

            case PENALTY_FEE_RENTAL:
                handleFailedPenaltyFee(payment);
                break;

            case RESERVATION:
                handleFailedReservation(payment);
                break;

            case RENTAL:
                handleFailedRental(payment);
                break;

            default:
                log.warn("Unhandled payment type for failed payment: {}", type);
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
        }
    }

    // Success handlers
    private void handleSuccessfulReservation(Payment payment) {
        Deposit deposit = payment.getDeposit();
        if (deposit != null) {
            deposit.setStatus(DepositStatus.HOLD);
            depositRepository.save(deposit);

            Reservation reservation = deposit.getReservation();
            if (reservation != null) {
                reservation.setStatus(ReservationStatus.CONFIRM);
                if (reservation.getCode() == null || reservation.getCode().isEmpty()) {
                    reservation.setCode(generateCode());
                }
                reservationRepository.save(reservation);
                emailService.sendReservationCodeEmail(reservation);
                log.info("Reservation confirmed: {}", reservation.getCode());
            }
            log.info("Reservation payment processed: {}", payment.getId());
        }
    }

    private void handleSuccessfulRental(Payment payment) {
        Deposit deposit = payment.getDeposit();
        Rental rental = payment.getRental();
        if (deposit != null && rental != null) {
            deposit.setStatus(DepositStatus.HOLD);
            depositRepository.save(deposit);
            rental.setStatus(RentalStatus.CONFIRM);
            rentalRepository.save(rental);
            log.info("Rental confirmed: {}", rental.getId());
            log.info("Rental payment processed: {}", payment.getId());
        }
    }

    private void handleSuccessfulPenaltyFee(Payment payment) {
        Rental rental = payment.getRental();
        if (rental != null) {
            rental.setStatus(RentalStatus.COMPLETED);
            rentalRepository.save(rental);
            log.info("Penalty fee rental payment processed: {}", payment.getId());
        }
    }

    private void handleSuccessfulExtension(Payment payment) {
        Rental rental = payment.getRental();
        if (rental != null && rental.getPendingEndTime() != null) {
            rental.setEndTime(rental.getPendingEndTime());
            rental.setRentFee(rental.getPendingRentFee());
            rental.setPendingEndTime(null);
            rental.setPendingRentFee(null);
            rental.setStatus(RentalStatus.CONFIRM);
            rentalRepository.save(rental);
            log.info("Rental extension payment processed: {}", payment.getId());
        }
    }

    // Failure handlers
    private void handleFailedExtension(Payment payment) {
        Rental rental = payment.getRental();
        if (rental != null) {
            rental.setPendingEndTime(null);
            rental.setPendingRentFee(null);
            rental.setStatus(RentalStatus.CONFIRM);
            rentalRepository.save(rental);
            log.info("Reverted failed extension for rental: {}", rental.getId());
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    private void handleFailedPenaltyFee(Payment payment) {
        Rental rental = payment.getRental();
        if (rental != null) {
            rental.setStatus(RentalStatus.PENDING_FEE);
            rentalRepository.save(rental);
            log.info("Reverted penalty fee payment for rental: {}", rental.getId());
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    private void handleFailedReservation(Payment payment) {
        Deposit deposit = payment.getDeposit();
        if (deposit != null) {
            deposit.setStatus(DepositStatus.FAILED);
            paymentRepository.save(payment);
            Reservation reservation = deposit.getReservation();
            if (reservation != null) {
                reservation.setStatus(ReservationStatus.FAILED);
                reservationRepository.save(reservation);
                log.info("Deleted failed reservation: {}", reservation.getId());
            }
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    private void handleFailedRental(Payment payment) {
        Deposit deposit = payment.getDeposit();
        if (deposit != null) {
            deposit.setStatus(DepositStatus.FAILED);
            Rental rental = deposit.getRental();
            depositRepository.save(deposit);
            if (rental != null) {
                rental.setStatus(RentalStatus.PENDING);
                rentalRepository.save(rental);
                log.info("Deleted failed rental: {}", rental.getId());
            }
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) {
        log.info("Processing refund for txnRef: {}", request.getTxnRef());

        try {
            // Find original payment
            Payment originalPayment = paymentRepository.findByTxnRef(request.getTxnRef())
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

            if(originalPayment.getTransactionNo().equals("999999")) {
                String refundTxnRef = "REFUND" + System.currentTimeMillis() +
                        (100000 + new Random().nextInt(900000));

                Payment refundPayment = Payment.builder()
                        .amount(Double.parseDouble(String.valueOf(request.getAmount())))
                        .method(PaymentMethod.VNPAY)
                        .status(PaymentStatus.SUCCESS)
                        .type(PaymentType.REFUND)
                        .txnRef(refundTxnRef)
                        .description("Hoan tien giao dich " + request.getTxnRef())
                        .responseCode("PostMan Test")
                        .transactionNo("999999")
                        .bankCode(originalPayment.getBankCode())
                        .payDate(LocalDateTime.now())
                        .user(originalPayment.getUser())
                        .rental(originalPayment.getRental())
                        .deposit(originalPayment.getDeposit())
                        .build();

                paymentRepository.save(refundPayment);
                log.info("Refund payment created successfully: {}", refundTxnRef);

                List<Deposit> releaseDeposit = new ArrayList<>();
                if(originalPayment.getDeposit() != null){
                    releaseDeposit.add(originalPayment.getDeposit());
                }
                if(originalPayment.getRental().getReservation() != null){
                    releaseDeposit.add(originalPayment.getRental().getReservation().getDeposit());
                }

                for(Deposit deposit : releaseDeposit){
                    deposit.setStatus(DepositStatus.RELEASED);
                    depositRepository.save(deposit);
                }

                return paymentMapper.toPaymentResponse(refundPayment);
            }

            // Validate payment can be refunded
            if (originalPayment.getStatus() != PaymentStatus.SUCCESS) {
                log.error("Payment cannot be refunded. Status: {}", originalPayment.getStatus());
                throw new AppException(ErrorCode.PAYMENT_CANNOT_BE_REFUNDED);
            }

            // Check if already refunded
            boolean alreadyRefunded = paymentRepository.existsByDepositIdAndTypeAndStatus(
                    originalPayment.getDeposit().getId(),
                    PaymentType.REFUND,
                    PaymentStatus.SUCCESS
            );

            if (alreadyRefunded) {
                log.warn("Refund already processed for txnRef: {}", request.getTxnRef());
                throw new AppException(ErrorCode.REFUND_ALREADY_PROCESSED);
            }

            // Prepare refund request
            String vnp_RequestId = vnPayConfig.generateTxnRef();
            String vnp_Version = "2.1.0";
            String vnp_Command = "refund";
            String vnp_CreateBy = "system";
            String vnp_TransactionType = "03";

            String vnp_TransactionDate = originalPayment.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String refundAmount = String.valueOf((long) (Double.parseDouble(String.format("%.2f", request.getAmount()))));

            Map<String, String> params = new LinkedHashMap<>();
            params.put("vnp_RequestId", vnp_RequestId);
            params.put("vnp_Version", vnp_Version);
            params.put("vnp_Command", vnp_Command);
            params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
            params.put("vnp_TransactionType", vnp_TransactionType);
            params.put("vnp_TxnRef", request.getTxnRef());
            params.put("vnp_Amount", refundAmount);
            params.put("vnp_OrderInfo", "Hoan tien giao dich");
            params.put("vnp_TransactionNo", originalPayment.getTransactionNo());
            params.put("vnp_TransactionDate", vnp_TransactionDate);
            params.put("vnp_CreateBy", vnp_CreateBy);
            params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            params.put("vnp_IpAddr", request.getIpAddr());

            // Build secure hash
            String data = String.join("|",
                    params.get("vnp_RequestId"),
                    params.get("vnp_Version"),
                    params.get("vnp_Command"),
                    params.get("vnp_TmnCode"),
                    params.get("vnp_TransactionType"),
                    params.get("vnp_TxnRef"),
                    params.get("vnp_Amount"),
                    params.get("vnp_TransactionNo"),
                    params.get("vnp_TransactionDate"),
                    params.get("vnp_CreateBy"),
                    params.get("vnp_CreateDate"),
                    params.get("vnp_IpAddr"),
                    params.get("vnp_OrderInfo")
            );

            String vnp_SecureHash = vnPayConfig.hmacSHA512(
                    vnPayConfig.getVnp_HashSecret(),
                    data
            );
            params.put("vnp_SecureHash", vnp_SecureHash);

            // Send refund request to VNPay
            log.info("Sending refund request to VNPay API");
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    vnPayConfig.getVnp_ApiUrl(),
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            log.info("VNPay refund response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.REFUND_RESPONSE_NOT_FOUND);
            }

            // Parse response
            HashMap<String, String> responseParams = new Gson().fromJson(responseBody, HashMap.class);

            String responseCode = responseParams.get("vnp_ResponseCode");
            if (responseCode == null) {
                throw new AppException(ErrorCode.REFUND_RESPONSE_INVALID);
            }else if ("94".equals(responseCode)) {
                log.warn("Refund failed for txnRef: {} with code: {}", responseParams.get("vnp_TxnRef"), responseCode);
                originalPayment.setTxnRef(vnPayConfig.generateTxnRef());
                paymentRepository.save(originalPayment);
                throw new AppException(ErrorCode.REFUND_IS_PROCESSING);
            }else if ("91".equals(responseCode)) {
                log.warn("Refund failed for txnRef: {} with code: {}", responseParams.get("vnp_TxnRef"), responseCode);
                throw new AppException(ErrorCode.REFUND_IS_NOT_FOUND);
            } else if(!"00".equals(responseCode)){
                log.error("Refund failed with response code: {}", responseCode);
                throw new AppException(ErrorCode.REFUND_FAILED);
            }

            // Create refund payment record
            String refundTxnRef = "REFUND" + System.currentTimeMillis() +
                    (100000 + new Random().nextInt(900000));

            LocalDateTime refundDate = parseRefundDate(responseParams.get("vnp_PayDate"));

            Payment refundPayment = Payment.builder()
                    .amount(Double.parseDouble(refundAmount))
                    .method(PaymentMethod.VNPAY)
                    .status(PaymentStatus.SUCCESS)
                    .type(PaymentType.REFUND)
                    .txnRef(refundTxnRef)
                    .description("Hoan tien giao dich " + request.getTxnRef())
                    .responseCode(responseCode)
                    .transactionNo(responseParams.get("vnp_TransactionNo"))
                    .bankCode(originalPayment.getBankCode())
                    .payDate(refundDate)
                    .user(originalPayment.getUser())
                    .rental(originalPayment.getRental())
                    .deposit(originalPayment.getDeposit())
                    .build();

            paymentRepository.save(refundPayment);
            log.info("Refund payment created successfully: {}", refundTxnRef);

            return paymentMapper.toPaymentResponse(refundPayment);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.REFUND_FAILED);
        }
    }

    private LocalDateTime parseRefundDate(String dateStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                log.warn("Cannot parse refund date: {}", dateStr);
            }
        }
        return LocalDateTime.now();
    }

    @Transactional
    public TransactionResponse queryTransaction(String txnRef, HttpServletRequest request) {
        log.info("Processing query transaction for txnRef: {}", txnRef);

        try {
            // Find original payment
            Payment originalPayment = paymentRepository.findByTxnRef(txnRef)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

            // Prepare refund request
            String vnp_RequestId = vnPayConfig.generateTxnRef();
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";

            String vnp_TransactionDate = originalPayment.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Map<String, String> params = new LinkedHashMap<>();
            params.put("vnp_RequestId", vnp_RequestId);
            params.put("vnp_Version", vnp_Version);
            params.put("vnp_Command", vnp_Command);
            params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
            params.put("vnp_TxnRef", txnRef);
            params.put("vnp_OrderInfo", "Kiem tra giao dich");
            params.put("vnp_TransactionNo", originalPayment.getTransactionNo());
            params.put("vnp_TransactionDate", vnp_TransactionDate);
            params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            params.put("vnp_IpAddr",request.getRemoteAddr());

            // Build secure hash
            String data = String.join("|",
                    params.get("vnp_RequestId"),
                    params.get("vnp_Version"),
                    params.get("vnp_Command"),
                    params.get("vnp_TmnCode"),
                    params.get("vnp_TxnRef"),
                    params.get("vnp_TransactionDate"),
                    params.get("vnp_CreateDate"),
                    params.get("vnp_IpAddr"),
                    params.get("vnp_OrderInfo")
            );

            String vnp_SecureHash = vnPayConfig.hmacSHA512(
                    vnPayConfig.getVnp_HashSecret(),
                    data
            );
            params.put("vnp_SecureHash", vnp_SecureHash);

            // Send query request to VNPay
            log.info("Sending query request to VNPay API");
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    vnPayConfig.getVnp_ApiUrl(),
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            log.info("VNPay query response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.QUERY_RESPONSE_NOT_FOUND);
            }

            // Parse response
            Gson gson = new Gson();
            TransactionResponse transactionResponse = gson.fromJson(responseBody, TransactionResponse.class);

            if(transactionResponse.getResponseCode() == null){
                throw new AppException(ErrorCode.QUERY_RESPONSE_NOT_FOUND);
            }else if(!"00".equals(transactionResponse.getResponseCode())){
                log.error("Query transaction failed with response code: {}", transactionResponse.getResponseCode());
                throw new AppException(ErrorCode.QUERY_FAILED);
            }
            return transactionResponse;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.QUERY_FAILED);
        }
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Rental rental = null;
        if (request.getRentalId() != null) {
            rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
            if(rental.getStatus() != RentalStatus.PENDING){
                throw new AppException(ErrorCode.RENTAL_CANNOT_BE_PAID);
            }
        }

        Deposit deposit = null;
        if (request.getDepositId() != null) {
            deposit = depositRepository.findById(request.getDepositId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            if(deposit.getStatus() != DepositStatus.PENDING){
                throw new AppException(ErrorCode.DEPOSIT_CANNOT_BE_PAID);
            }
        }

        Payment payment = Payment.builder()
                .user(user)
                .rental(rental)
                .deposit(deposit)
                .amount(request.getAmount())
                .description(request.getDescription())
                .method(PaymentMethod.CASH)
                .type(request.getPaymentType())
                .status(PaymentStatus.SUCCESS)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse updatePayment(UpdatePaymentRequest request, Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setType(request.getType());
        payment.setStatus(request.getStatus());
        payment.setDescription(request.getDescription());

        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            if (payment.getType() == PaymentType.RESERVATION && payment.getDeposit() != null) {
                Deposit deposit = depositRepository.findById(payment.getDeposit().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
                Reservation reservation = reservationRepository.findById(deposit.getReservation().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

                deposit.setStatus(DepositStatus.HOLD);
                reservation.setStatus(ReservationStatus.CONFIRM);

                depositRepository.save(deposit);
                reservationRepository.save(reservation);

            } else if (payment.getType() == PaymentType.RENTAL && payment.getDeposit() != null && payment.getRental() != null) {
                Deposit deposit = depositRepository.findById(payment.getDeposit().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
                Rental rental = rentalRepository.findById(payment.getRental().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

                deposit.setStatus(DepositStatus.HOLD);
                rental.setStatus(RentalStatus.CONFIRM);

                depositRepository.save(deposit);
                rentalRepository.save(rental);
            }
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(savedPayment);
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));
        payment.setDelete(true);
        paymentRepository.save(payment);
    }

    public List<PaymentResponse> getAllPayment() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));
        return paymentMapper.toPaymentResponse(payment);
    }

    public PaymentResponse findByTxnRef(String txnRef) {
        Payment payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));
        return paymentMapper.toPaymentResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        if (status == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
        }
        return paymentRepository.getPaymentsByStatus(status).stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByMethod(PaymentMethod method) {
        if (method == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
        }
        return paymentRepository.getPaymentsByMethod(method).stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByType(PaymentType type) {
        if (type == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
        }
        return paymentRepository.getPaymentsByType(type).stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    private String generateCode() {
        Random random = new Random();
        int code = 0;
        do{
            code = random.nextInt(900000) + 100000;
        }while(reservationRepository.findByCode(String.valueOf(code)).isPresent());
        return String.valueOf(code);
    }

    public PaymentResponse getPaymentByRentalId(Long rentalId) {
        Payment payment = paymentRepository.findTopByTypeAndRentalIdOrderByCreatedAtDesc(PaymentType.RENTAL, rentalId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));
        return paymentMapper.toPaymentResponse(payment);
    }
}