package com.swp391.e_Motion_be.dto.responses.vehicle;

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
    private String status;
    private String category;
    private int seats;
    private Double pricePerDay;
    private Double consumptionRate;
    private Double batteryCapacity;
    private Long stationId;
    private String city;
}
