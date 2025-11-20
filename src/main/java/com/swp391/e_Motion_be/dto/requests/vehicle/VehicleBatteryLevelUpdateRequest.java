package com.swp391.e_Motion_be.dto.requests.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleBatteryLevelUpdateRequest {
    @NotNull(message = "ID phương tiện không được để trống")
    private Long vehicleId;
    @Min(value = 0, message = "Mức pin phải lớn hơn hoặc bằng 0")
    @Max(value = 100, message = "Mức pin phải nhỏ hơn hoặc bằng 100")
    private int batteryLevel;
}
