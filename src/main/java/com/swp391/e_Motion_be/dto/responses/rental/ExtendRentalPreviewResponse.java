package com.swp391.e_Motion_be.dto.responses.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtendRentalPreviewResponse {
    private double additionalFee;
    private double newRentFee;
    private double currentRentFee;
    private long additionalHours;
    private LocalDateTime newReturnTime;
}
