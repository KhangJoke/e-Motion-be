package com.swp391.e_Motion_be.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogResponse {
    private Long id;
    private Map<String, Double> repairCost;
    private LocalDateTime createdAt;
    private Long vehicleId;
    private Long staffId;
    private String rentalId;
}
