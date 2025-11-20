package com.swp391.e_Motion_be.dto.requests.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeRequest {
    private Long VehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean rental;
}
