package com.swp391.e_Motion_be.dto.requests.VehicleLog;

import com.swp391.e_Motion_be.enums.VehicleLogType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogUpdateRequest {
    @NotNull(message = "VehicleLogType is required")
    private VehicleLogType vehicleLogType;

    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "VehicleId is required")
    private Long vehicleId;

    @NotNull(message = "UserId is required (Staff)")
    private Long userId;

}
