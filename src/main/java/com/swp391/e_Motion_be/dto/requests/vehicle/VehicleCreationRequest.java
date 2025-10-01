package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCreationRequest {

    @NotBlank(message = "Vehicle name is required")
    private String name;

    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Vehicle status is required")
    private VehicleStatus vehicleStatus;

    @NotNull(message = "Depsoit fee status is required")
    private double depositFee;

    @NotNull(message = "Seats is required")
    @Positive(message = "Seats must be positive")
    private int seats;

    @NotNull(message = "Price per hour is required")
    @Positive(message = "Price per hour must be positive")
    private Double pricePerHour;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price per day must be positive")
    private Double pricePerDay;

    @NotNull(message = "The consumption rate is required")
    @Positive(message = "Consumption rate must be positive")
    private Double consumptionRate;

    @NotNull(message = "Battery level is required")
    @DecimalMin(value = "0.0", message = "Battery level cannot be less than 0")
    @DecimalMax(value = "1.0", message = "Battery level cannot be greater than 1")
    private Double batteryLevel;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Battery capacity must be positive")
    private Double batteryCapacity;

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotNull(message = "Date for last maintenance is required")
    private LocalDateTime lastMaintenance;

    @NotNull(message = "Station ID is required")
    private Long stationId; // <--Station by ID
}
