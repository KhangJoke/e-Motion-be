package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.config.VNPayConfig;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PaymentRequest;
import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.RefundResponse;
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
import com.swp391.e_Motion_be.service.auth.EmailService;
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
import java.util.stream.Collectors;

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
    public String createPaymentUrl(CreatePaymentUrlRequest request, String ipAddr) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = vnPayConfig.generateTxnRef();

        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Rental rental = null;
        if (request.getRentalId() != null) {
            rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        }

        Deposit deposit = depositRepository.findById(request.getDepositId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));

        Payment payment = Payment.builder()
                .user(user)
                .rental(rental)
                .deposit(deposit)
                .txnRef(vnp_TxnRef)
                .amount(request.getAmount())
                .description(request.getDescription())
                .method(PaymentMethod.VNPAY)
                .type(request.getRentalId() == null ? PaymentType.RESERVATION : PaymentType.RENTAL)
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(request.getAmount() * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", request.getDescription());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);

            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return vnPayConfig.getVnp_PayUrl() + "?" + query.toString();
    }

    @Transactional
    public PaymentResponse handleReturn(Map<String, String> params) throws Exception {
        String vnp_SecureHash = params.get("vnp_SecureHash");

        if (!vnPayConfig.validateSignature(params, vnp_SecureHash)) {
            throw new AppException(ErrorCode.VNPAY_KEY_INVALID);
        }

        String vnp_TxnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");
        String payDate = params.get("vnp_PayDate");

        Payment payment = paymentRepository.findByTxnRef(vnp_TxnRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

        // Prevent duplicate processing
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment already processed: {}", vnp_TxnRef);
            return paymentMapper.toPaymentResponse(payment);
        }

        Deposit deposit = null;
        Rental rental = null;
        Reservation reservation = null;

        if (payment.getDeposit() != null) {
            deposit = depositRepository.findById(payment.getDeposit().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            if (payment.getType() == PaymentType.RESERVATION) {
                reservation = reservationRepository.findById(deposit.getReservation().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
            }
        }

        if (payment.getRental() != null) {
            rental = rentalRepository.findById(payment.getRental().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        }

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

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            if (deposit != null && payment.getType() == PaymentType.RESERVATION) {
                deposit.setStatus(DepositStatus.HOLD);
                reservation.setStatus(ReservationStatus.CONFIRM);
                if (reservation.getCode() == null || reservation.getCode().isEmpty()) {
                    reservation.setCode(generateCode());
                }
                emailService.sendReservationCodeEmail(reservation);
                depositRepository.save(deposit);
                reservationRepository.save(reservation);

            } else if (deposit != null && rental != null && payment.getType() == PaymentType.RENTAL) {
                deposit.setStatus(DepositStatus.HOLD);
                rental.setStatus(RentalStatus.CONFIRM);
                depositRepository.save(deposit);
                rentalRepository.save(rental);
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) throws Exception {
        log.info("Processing refund for txnRef: {}", request.getTxnRef());

        try {
            // Find original payment
            Payment originalPayment = paymentRepository.findByTxnRef(request.getTxnRef())
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

            // Validate payment can be refunded
            if (originalPayment.getStatus() != PaymentStatus.SUCCESS) {
                throw new AppException(ErrorCode.PAYMENT_CANNOT_BE_REFUNDED);
            }

            String vnp_RequestId = String.valueOf(System.currentTimeMillis());
            String vnp_Version = "2.1.0";
            String vnp_Command = "refund";
            String vnp_CreateBy = "system";
            String vnp_TransactionType = request.isFullRefund() ? "03" : "02";

            String vnp_TransactionDate = originalPayment.getPayDate()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Map<String, String> params = new LinkedHashMap<>();
            params.put("vnp_RequestId", vnp_RequestId);
            params.put("vnp_Version", vnp_Version);
            params.put("vnp_Command", vnp_Command);
            params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
            params.put("vnp_TransactionType", vnp_TransactionType);
            params.put("vnp_TxnRef", request.getTxnRef());
            params.put("vnp_Amount", String.valueOf(originalPayment.getAmount()));
            params.put("vnp_OrderInfo", "Hoan tien giao dich " + request.getTxnRef());
            params.put("vnp_TransactionNo", originalPayment.getTransactionNo());
            params.put("vnp_TransactionDate", vnp_TransactionDate);
            params.put("vnp_CreateBy", vnp_CreateBy);
            params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            params.put("vnp_IpAddr", request.getIpAddr());

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

            log.info("Sending refund request to VNPay");
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> respEntity = restTemplate.postForEntity(
                    vnPayConfig.getVnp_ApiUrl(), entity, String.class);

            String body = respEntity.getBody();
            log.info("VNPay refund response: {}", body);

            if (body == null || body.isEmpty()) {
                throw new AppException(ErrorCode.REFUND_RESPONSE_NOT_FOUND);
            }

            Map<String, String> respEntityParams = Arrays.stream(body.split("&"))
                    .map(s -> s.split("=", 2))
                    .filter(a -> a.length == 2)
                    .collect(Collectors.toMap(a -> a[0], a -> a[1]));

            RefundResponse response = new RefundResponse();
            response.setResponseCode(respEntityParams.get("vnp_ResponseCode"));
            response.setMessage(respEntityParams.get("vnp_Message"));
            response.setTxnRef(respEntityParams.get("vnp_TxnRef"));
            response.setAmount(respEntityParams.get("vnp_Amount"));
            response.setTransactionNo(respEntityParams.get("vnp_TransactionNo"));
            response.setCreateDate(respEntityParams.get("vnp_CreateDate"));
            response.setResponseCode(respEntityParams.get("vnp_ResponseCode"));

            if (response.getResponseCode() == null) {
                throw new AppException(ErrorCode.REFUND_RESPONSE_INVALID);
            }

            // Create refund payment record with UNIQUE txnRef
            String refundTxnRef = "REFUND" + System.currentTimeMillis() +
                    (100000 + new Random().nextInt(900000));

            // Use original amount if VNPay doesn't return it
            long refundAmount = originalPayment.getAmount();
            if (response.getAmount() != null && !response.getAmount().isEmpty()) {
                try {
                    refundAmount = Long.parseLong(response.getAmount()) / 100;
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse refund amount: {}", response.getAmount());
                }
            }

            LocalDateTime refundDate = LocalDateTime.now();
            if (response.getCreateDate() != null && !response.getCreateDate().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    refundDate = LocalDateTime.parse(response.getCreateDate(), formatter);
                } catch (Exception e) {
                    log.warn("Cannot parse refund date: {}", response.getCreateDate());
                }
            }

            Payment refundPayment = Payment.builder()
                    .amount(refundAmount)
                    .method(PaymentMethod.VNPAY)
                    .status("00".equals(response.getResponseCode())
                            ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                    .type(PaymentType.REFUND)
                    .txnRef(refundTxnRef)
                    .description("Hoan tien giao dich " + request.getTxnRef())
                    .responseCode(response.getResponseCode())
                    .transactionNo(response.getTransactionNo())
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

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Rental rental = null;
        if (request.getRentalId() != null) {
            rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        }

        Deposit deposit = null;
        if (request.getDepositId() != null) {
            deposit = depositRepository.findById(request.getDepositId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        }

        Payment payment = Payment.builder()
                .user(user)
                .rental(rental)
                .deposit(deposit)
                .amount(request.getAmount())
                .description(request.getDescription())
                .method(request.getMethod())
                .type(request.getType())
                .status(request.getStatus())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse updatePayment(PaymentRequest request, Long id) {
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
        paymentRepository.delete(payment);
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
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}