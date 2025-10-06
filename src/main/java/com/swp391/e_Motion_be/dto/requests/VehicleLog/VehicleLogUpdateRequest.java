package com.swp391.e_Motion_be.dto.requests.VehicleLog;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogUpdateRequest {
    @NotNull(message = "Repair cost list is require")
    private Map<String, Double> repairCost;

    @NotNull(message = "Cost is required")
    @PositiveOrZero(message = "Cost must be zero or positive")
    private Double cost;

    @NotNull(message = "VehicleId is required")
    private Long vehicleId;

    @NotNull(message = "UserId is required (Staff)")
    private Long userId;

    @NotNull(message = "RentalId is required")
    private Long rentalId;
}
