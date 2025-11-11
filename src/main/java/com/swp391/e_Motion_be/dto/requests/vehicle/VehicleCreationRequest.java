package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleCreationRequest;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleCategory;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCreationRequest {

    @NotBlank(message = "Vehicle name is required")
    private String name;

    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private VehicleCategory category;

    @NotNull(message = "Brand is required")
    private VehicleBrand brand;

    @NotNull(message = "Vehicle status is required")
    private VehicleStatus status;

    @NotNull(message = "Depsoit fee status is required")
    private double depositFee;

    @NotNull(message = "Seats is required")
    @Positive(message = "Seats must be positive")
    private int seats;

    @NotNull(message = "Price 4 hours hour is required")
    @Positive(message = "Price 4 hours hour must be positive")
    private Double pricePer4Hours;

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

    @NotNull(message = "Images are required")
    private List<ImgVehicleCreationRequest> images;
}
