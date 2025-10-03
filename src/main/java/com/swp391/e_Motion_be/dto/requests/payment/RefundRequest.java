package com.swp391.e_Motion_be.dto.requests.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {
    @NotBlank(message = "Ip Address must not be blank")
    private String ipAddr;
    @NotBlank(message = "txnRef must not be blank")
    private String txnRef;
    @NotNull(message = "Refund full or part must not be blank")
    private boolean fullRefund;
}
