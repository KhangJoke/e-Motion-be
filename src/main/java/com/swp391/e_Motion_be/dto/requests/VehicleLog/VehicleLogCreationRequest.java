package com.swp391.e_Motion_be.dto.requests.VehicleLog;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogCreationRequest {

    @NotNull(message = "Repair cost list is require")
    private Map<String, Double> repairCost;

    @NotNull(message = "Fee is required")
    @Positive(message = "Fee must be positive")
    private Double fee;

    @NotNull(message = "VehicleId is required")
    private Long vehicleId;

    @NotNull(message = "StaffId is required (Staff)")
    private Long staffId;

    @NotNull(message = "RentalId is required")
    private Long rentalId;
}
