package com.swp391.e_Motion_be.dto.email;

import com.swp391.e_Motion_be.enums.payment.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PaymentEmailRequest {
    private String subject;
    private String message;
    private PaymentType paymentType;
    private String paymentStatus;
    private String statusColor;
    private List<PaymentItem> items;
    private Map<String, Double> vehicleDamages;
    private double vehicleDamagesTotal;
    private double total;
    private double totalDeposit;
    private double refundAmount;
}
