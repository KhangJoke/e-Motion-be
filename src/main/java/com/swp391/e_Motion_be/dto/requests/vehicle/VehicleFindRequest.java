package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.enums.StationCity;
import com.swp391.e_Motion_be.validator.validateFindVehicleRequest.ValidTimeRequest;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ValidTimeRequest
public class VehicleFindRequest {
    @NotNull(message = "City must not be null")
    StationCity city;
    @NotNull(message = "Start time must not be null")
    @Future(message = "Start time must be in the future")
    LocalDateTime startTime;
    @NotNull(message = "End time must not be null")
    @Future(message = "End time must be in the future")
    LocalDateTime endTime;
}
