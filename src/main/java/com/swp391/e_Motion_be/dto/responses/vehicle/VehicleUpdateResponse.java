package com.swp391.e_Motion_be.dto.responses.vehicle;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleCreationRequest;
import com.swp391.e_Motion_be.dto.responses.ImgVehicleResponse;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleUpdateResponse {
    private Long id;
    private String name;
    private String description;
    private VehicleCategory category;
    private VehicleBrand brand;
    private double depositFee;
    private int seats;
    private int point;
    private Double pricePer4Hours;
    private Double consumptionRate;
    private int batteryLevel;
    private Double batteryCapacity;
    private String plateNumber;
    private Long stationId;
    private List<ImgVehicleResponse> images;
}
