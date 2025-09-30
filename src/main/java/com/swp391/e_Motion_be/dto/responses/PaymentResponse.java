package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.PaymentMethod;
import com.swp391.e_Motion_be.enums.PaymentStatus;
import com.swp391.e_Motion_be.enums.PaymentType;
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
    private PaymentMethod method;
    private PaymentType type;
    private PaymentStatus status;
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
