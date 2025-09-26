package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.VehicleLogType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogResponse {
    private VehicleLogType vehicleLogType;
    private String description;
    private LocalDateTime createdAt;
    private Long vehicleId;
    private Long userId;
}
