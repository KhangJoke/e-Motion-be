package com.swp391.e_Motion_be.dto.requests.rental;

import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckOutProcessResponse {
    private String processStatus; // "PAYMENT_REQUIRED" hoặc "COMPLETED"
    private String paymentUrl;
    private RentalResponse rental;
}