package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.config.VNPayConfig;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PaymentRequest;
import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.RefundResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.*;
import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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

    public String createPaymentUrl(CreatePaymentUrlRequest request, String ipAddr) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = vnPayConfig.generateTxnRef(); // random mã giao dịch
        Rental rental = new Rental();
        Deposit deposit = new Deposit();
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        rental.setId(request.getRentalId());
        deposit.setId(request.getDepositId());

        // Lưu Payment ở trạng thái PENDING
        Payment payment = Payment.builder()
                .user(user)
                .rental(rental)
                .deposit(deposit)
                .txnRef(vnp_TxnRef)
                .amount(request.getAmount())
                .description(request.getDescription())
                .method(PaymentMethod.VNPAY)
                .type(request.getRentalId()==null? PaymentType.RESERVATION : PaymentType.RENTAL)
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
        vnp_Params.put("vnp_OrderInfo",request.getDescription());
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


    public String handleReturn(HttpServletRequest request) {
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String bankCode = request.getParameter("vnp_BankCode");
        String payDate = request.getParameter("vnp_PayDate");

        Payment payment = paymentRepository.findByTxnRef(vnp_TxnRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

        Deposit deposit = null;
        Rental rental = null;
        Reservation reservation = null;

        if (payment.getDeposit() != null) {
            deposit = depositRepository.findById(payment.getDeposit().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            if (payment.getType() == PaymentType.RESERVATION) {
                reservation = reservationRepository.findByCode(deposit.getReservation().getCode())
                        .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
            }
        }

        if (payment.getRental() != null) {
            rental = rentalRepository.findById(payment.getRental().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        }

        // Update các field trả về từ VNPay
        payment.setResponseCode(responseCode);
        payment.setTransactionNo(transactionNo);
        payment.setBankCode(bankCode);
        payment.setPayDate(LocalDateTime.parse(payDate));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            if (deposit != null && payment.getType() == PaymentType.RESERVATION) {
                deposit.setStatus(DepositStatus.HOLD);
                reservation.setStatus(ReservationStatus.CONFIRM);
                depositRepository.save(deposit);
                reservationRepository.save(reservation);

            } else if (deposit != null && rental != null && payment.getType() == PaymentType.RENTAL) {
                deposit.setStatus(DepositStatus.HOLD);
                rental.setStatus(RentalStatus.CONFIRM);
                depositRepository.save(deposit);
                rentalRepository.save(rental);

            }
            // TH3: không làm gì thêm, chỉ update status = SUCCESS
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        return payment.getStatus().name();
    }

    public PaymentResponse refundPayment(RefundRequest request) throws Exception {
        String vnp_RequestId = String.valueOf(System.currentTimeMillis()); // ID request duy nhất
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_CreateBy = "system"; // Người thực hiện refund
        String vnp_TransactionType = request.isFullRefund()? "03" : "02"; // 02 = partial, 03 = full refund

        // ---- Build params ----
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", vnp_RequestId);
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        params.put("vnp_TransactionType", vnp_TransactionType);
        params.put("vnp_TxnRef", request.getTxnRef());
        params.put("vnp_Amount", String.valueOf(request.getAmount() * 100)); // VNPay yêu cầu VNĐ x 100
        params.put("vnp_OrderInfo", "Hoan tien giao dich " + request.getTxnRef());
        params.put("vnp_TransactionNo", ""); // Có thể để trống
        params.put("vnp_TransactionDate", request.getTransactionDate()); // Giao dịch gốc
        params.put("vnp_CreateBy", vnp_CreateBy);
        params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        // ---- Build data để ký hash ----
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!data.isEmpty()) {
                data.append("&");
            }
            data.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // ---- Sinh SecureHash ----
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), data.toString());
        params.put("vnp_SecureHash", vnp_SecureHash);

        // ---- Gửi request sang VNPay ----
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
        RefundResponse response = restTemplate.postForEntity(vnPayConfig.getVnp_ApiUrl(), entity, RefundResponse.class).getBody();
        if(response==null) {
            throw new AppException(ErrorCode.REFUND_RESPONSE_NOT_FOUND);
        }
        // ---- Trả về JSON kết quả ----
        Payment depositPayment = paymentRepository.findByTxnRef(response.getTxnRef())
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_PAYMENT_NOT_FOUND));

        Payment refundDepositPayment = Payment.builder()
                .amount(Long.parseLong(response.getAmount()))
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.SUCCESS)
                .type(PaymentType.REFUND)
                .txnRef(response.getTxnRef())
                .description("Hoan tien giao dich " + request.getTxnRef())
                .responseCode(response.getResponseCode())
                .transactionNo(response.getTransactionNo())
                .bankCode(depositPayment.getBankCode())
                .payDate(LocalDateTime.parse(response.getCreateDate()))
                .user(depositPayment.getUser())
                .rental(depositPayment.getRental())
                .deposit(depositPayment.getDeposit())
                .build();
        paymentRepository.save(refundDepositPayment);

        return paymentMapper.toPaymentResponse(refundDepositPayment);
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        Rental rental = new Rental();
        Deposit deposit = new Deposit();
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        rental.setId(request.getRentalId());
        deposit.setId(request.getDepositId());

        // Lưu Payment ở trạng thái PENDING
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
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    public PaymentResponse updatePayment(PaymentRequest request, Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

        // update basic fields
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setType(request.getType());
        payment.setStatus(request.getStatus());
        payment.setDescription(request.getDescription());

        // xử lý theo 3 case
        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            if (payment.getType() == PaymentType.RESERVATION && payment.getDeposit() != null) {
                Deposit deposit = depositRepository.findById(payment.getDeposit().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
                Reservation reservation = reservationRepository.findByCode(deposit.getReservation().getCode())
                        .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

                deposit.setStatus(DepositStatus.HOLD);
                reservation.setStatus(ReservationStatus.CONFIRM);

                depositRepository.save(deposit);
                reservationRepository.save(reservation);

            } else if (payment.getType() == PaymentType.RENTAL && payment.getDeposit() != null && payment.getRental() != null) {
                Deposit deposit = depositRepository.findById(payment.getDeposit().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
                Rental rental = rentalRepository.getRentalById(payment.getRental().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

                deposit.setStatus(DepositStatus.HOLD);
                rental.setStatus(RentalStatus.CONFIRM);

                depositRepository.save(deposit);
                rentalRepository.save(rental);

            } else {
                // case 3: Other payment, không cần động đến deposit/rental
            }
        }

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS));

        paymentRepository.delete(payment);
    }

    public List<PaymentResponse> getAllPayment(){
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
        if(status != null) {
            return paymentRepository.getPaymentsByStatus(status).stream()
                    .map(paymentMapper::toPaymentResponse)
                    .toList();
        }
        throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
    }

    public List<PaymentResponse> getPaymentsByMethod(PaymentMethod method) {
        if(method != null) {
            return paymentRepository.getPaymentsByMethod(method).stream()
                    .map(paymentMapper::toPaymentResponse)
                    .toList();
        }
        throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
    }

    public List<PaymentResponse> getPaymentsByType(PaymentType type) {
        if(type != null) {
            return paymentRepository.getPaymentsByType(type).stream()
                    .map(paymentMapper::toPaymentResponse)
                    .toList();
        }
        throw new AppException(ErrorCode.PAYMENT_NOT_EXISTS);
    }
}
