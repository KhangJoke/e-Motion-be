package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import lombok.Data;

@Data
public class VehicleStatusUpdateRequest {
    private VehicleStatus status;
}
