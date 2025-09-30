package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.config.VNPayConfig;
import com.swp391.e_Motion_be.dto.requests.payment.PaymentRequest;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.entity.Deposit;
import com.swp391.e_Motion_be.entity.Payment;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.PaymentMethod;
import com.swp391.e_Motion_be.enums.PaymentStatus;
import com.swp391.e_Motion_be.enums.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.repository.PaymentRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setResponseCode(responseCode);
        payment.setTransactionNo(transactionNo);
        payment.setBankCode(bankCode);
        payment.setPayDate(LocalDateTime.parse(payDate));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);

        return payment.getStatus().name();
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

        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setType(request.getType());
        payment.setStatus(request.getStatus());
        payment.setDescription(request.getDescription());

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
