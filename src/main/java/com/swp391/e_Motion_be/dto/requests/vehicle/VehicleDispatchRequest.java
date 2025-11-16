package com.swp391.e_Motion_be.dto.requests.vehicle;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDispatchRequest {
    @NotEmpty
    private List<Long> vehicleIds;
    @NotNull
    private Long stationId;
}
