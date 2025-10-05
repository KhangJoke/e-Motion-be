package com.swp391.e_Motion_be.dto.responses.vehicle;

import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleListResponse {

    private Long id;
    private String name;
    private String type;
    private VehicleStatus status;
    private String category;
    private int seats;
    private Double pricePerDay;
    private Double consumptionRate;
    private Double batteryCapacity;
    private Long stationId;
    private StationCity city;
    private String isMain;
}
