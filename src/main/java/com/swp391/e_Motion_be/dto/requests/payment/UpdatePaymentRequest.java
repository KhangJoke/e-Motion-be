package com.swp391.e_Motion_be.dto.requests.payment;

import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentRequest {
    @NotNull(message = "Payment Method must not be blank")
    private PaymentMethod method;
    @NotNull(message = "Payment Status must not be blank")
    private PaymentStatus status;
    @NotNull(message = "Payment Type must not be blank")
    private PaymentType type;
    @NotNull(message = "Amount must not be blank")
    private Long amount;
    @NotBlank(message = "Payment Description must not be blank")
    private String description;
    @NotBlank(message = "User Email must not be blank")
    private String userEmail;
    private Long depositId;
    private Long rentalId;
}
