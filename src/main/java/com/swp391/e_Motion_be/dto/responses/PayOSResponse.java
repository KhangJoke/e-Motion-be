package com.swp391.e_Motion_be.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOSResponse {
    private String checkoutUrl;
    private String qrCode;
    private String accountNumber;
    private String accountName;
    private String bin;
    private Long orderCode;
    private Double amount;
    private String description;
    private String status;
}
