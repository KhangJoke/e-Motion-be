package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long amount;
    private String method;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private String txnRef;
    private String description;

    // Các tham số VNPay trả về
    private String responseCode;  // vnp_ResponseCode
    private String transactionNo; // vnp_TransactionNo
    private String bankCode;      // vnp_BankCode
    private LocalDateTime payDate;// vnp_PayDate

    private String userEmail;
    private Long rentalId;
    private Long depositId;
}
