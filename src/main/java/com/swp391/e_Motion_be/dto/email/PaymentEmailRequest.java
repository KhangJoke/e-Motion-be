package com.swp391.e_Motion_be.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEmailRequest {
    private String subject;
    private String message;
    private String paymentStatus;
    private String statusColor;
    private double depositFee;
    private double rentalFee;
    private double penaltyFee;
    private double vehicleLogFee;
    private Map<String, Double> vehicleDamages;
    private double total;
}
