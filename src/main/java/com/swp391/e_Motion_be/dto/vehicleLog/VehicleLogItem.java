package com.swp391.e_Motion_be.dto.vehicleLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLogItem {
    private String description;
    private double cost;
}
