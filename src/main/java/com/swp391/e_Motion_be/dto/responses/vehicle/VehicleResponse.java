package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private String status;
    private int seats;
    private Double pricePerHour;
    private Double pricePerDay;
    private double depositFee;
    private Double consumptionRate;
    private Double batteryLevel;
    private Double batteryCapacity;
    private String plateNumber;
    private LocalDateTime lastMaintenance;
    private Long stationId;
}
