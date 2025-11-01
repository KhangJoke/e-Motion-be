package com.swp391.e_Motion_be.dto.requests.VehicleLog;

import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @NotNull(message = "staffId are required")
    private Long staffId;

    @NotNull(message = "RentalId is required")
    private Long rentalId;
}
