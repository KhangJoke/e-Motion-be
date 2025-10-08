package com.swp391.e_Motion_be.dto.requests.payment;

import com.swp391.e_Motion_be.enums.payment.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentUrlRequest {
    @NotNull(message = "Amount must not be blank")
    private double amount;
    private String description;
    @NotBlank(message = "User Email must not be blank")
    private String userEmail;
    @NotNull(message = "Payment Type must not be null")
    private PaymentType type;
    private Long depositId;
    private Long rentalId;
}
