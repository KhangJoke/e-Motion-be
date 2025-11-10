package com.swp391.e_Motion_be.dto.responses.vehicle;

import com.swp391.e_Motion_be.dto.responses.ImgVehicleResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationResponse;
import com.swp391.e_Motion_be.entity.ImgVehicle;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private VehicleStatus status;
    private VehicleBrand brand;
    private int seats;
    private Double pricePer4Hours;
    private Double pricePer8Hours;
    private Double pricePer12Hours;
    private Double pricePerDay;
    private Double depositFee;
    private Double consumptionRate;
    private Double batteryLevel;
    private Double batteryCapacity;
    private String plateNumber;
    private LocalDateTime lastMaintenance;
    private StationResponse station;
    private List<ImgVehicleResponse> images;
}
