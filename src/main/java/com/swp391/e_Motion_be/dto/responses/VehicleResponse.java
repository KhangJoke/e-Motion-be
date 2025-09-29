package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.VehicleStatus;
import com.swp391.e_Motion_be.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {

    private String name;
    private VehicleType vehicleType;
    private VehicleStatus vehicleStatus;
    private int seats;
    private Double pricePerHour;
    private Double pricePerDay;
    private Double consumptionRate;
    private Double batteryLevel;
    private Double batteryCapacity;
    private String plateNumber;
    private LocalDateTime lastMaintenance;
    private Long stationId;
}
