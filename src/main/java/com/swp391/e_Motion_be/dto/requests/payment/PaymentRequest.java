package com.swp391.e_Motion_be.dto.requests.payment;

import com.swp391.e_Motion_be.enums.PaymentMethod;
import com.swp391.e_Motion_be.enums.PaymentStatus;
import com.swp391.e_Motion_be.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NotNull(message = "Amount must not be blank")
    private Long amount;
    @NotNull(message = "Payment Method must not be blank")
    private PaymentMethod method;
    @NotNull(message = "Payment Status must not be blank")
    private PaymentStatus status;
    @NotNull(message = "Payment Type must not be blank")
    private PaymentType type;
    @NotBlank(message = "Payment Description must not be blank")
    private String description;
    @NotBlank(message = "User Email must not be blank")
    private String userEmail;
    private Long depositId;
    private Long rentalId;
}
