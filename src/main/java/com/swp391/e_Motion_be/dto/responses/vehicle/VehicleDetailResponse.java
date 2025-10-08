package com.swp391.e_Motion_be.dto.responses.vehicle;

import com.swp391.e_Motion_be.enums.station.StationCity;
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
    private String type;
    private String category;
    private VehicleStatus status;
    private int seats;
    private String pricePer4Hours;
    private String pricePer8Hours;
    private String pricePer12Hours;
    private String pricePerDay;
    private String depositFee;
    private Double consumptionRate;
    private Double batteryLevel;
    private Double batteryCapacity;
    private String plateNumber;
    private LocalDateTime lastMaintenance;
    private Long stationId;
    private StationCity city;
    private String address;
    private List<String> images;

}
