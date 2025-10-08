package com.swp391.e_Motion_be.dto.responses.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleSearchResponse {

    private List<VehicleListResponse> availableVehicles;
    private List<VehicleListResponse> unavailableVehicles;

}
