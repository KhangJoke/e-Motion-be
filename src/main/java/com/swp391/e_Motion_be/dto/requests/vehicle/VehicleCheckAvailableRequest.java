package com.swp391.e_Motion_be.dto.requests.vehicle;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCheckAvailableRequest {
    @NotNull(message = "Vehicle ID must not be null")
    private Long vehicleId;
    @NotNull(message = "Start time must not be null")
    @Future(message = "Start time must be in the future")
    LocalDateTime startTime;
    @NotNull(message = "End time must not be null")
    @Future(message = "End time must be in the future")
    LocalDateTime endTime;
}
