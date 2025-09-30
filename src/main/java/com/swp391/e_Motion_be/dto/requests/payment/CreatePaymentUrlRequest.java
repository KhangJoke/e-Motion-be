package com.swp391.e_Motion_be.dto.requests.payment;

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
    private Long amount;
    private String description;
    @NotBlank(message = "User Email must not be blank")
    private String userEmail;
    private Long depositId;
    private Long rentalId;
}
