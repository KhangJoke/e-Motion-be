package com.swp391.e_Motion_be.dto.requests.rental;

import com.swp391.e_Motion_be.validator.validateTimeVehicleRequest.ValidTimeRequest;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ValidTimeRequest
public class RentalCreateRequest {
    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be now or in the future")
    LocalDateTime startTime;
    @NotNull(message = "End time is required")
    LocalDateTime endTime;
    @NotNull(message = "Vehicle ID is required")
    Long vehicleId;
    @NotNull(message = "User email is required")
    String email;
    @NotNull(message = "Staff ID is required")
    Long staffId;
}
