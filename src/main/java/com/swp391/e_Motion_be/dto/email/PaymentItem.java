package com.swp391.e_Motion_be.dto.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentItem {
    private String label;
    private double amount;
}
