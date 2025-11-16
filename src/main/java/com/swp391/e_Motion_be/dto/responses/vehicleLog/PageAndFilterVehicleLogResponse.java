package com.swp391.e_Motion_be.dto.responses.vehicleLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterVehicleLogResponse {
    private List<VehicleLogResponse> content;
    private int totalPages;
}
