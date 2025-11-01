package com.swp391.e_Motion_be.dto.requests.VehicleLog;

import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogUpdateRequest {
    @NotNull(message = "Repair cost list is require")
    private List<VehicleLogItem> repairItems;

    @NotNull(message = "VehicleId is required")
    private Long vehicleId;

    @NotNull(message = "Images are required")
    private List<String> imgs;

    @NotNull(message = "UserId is required (Staff)")
    private Long userId;

    @NotNull(message = "RentalId is required")
    private Long rentalId;
}
