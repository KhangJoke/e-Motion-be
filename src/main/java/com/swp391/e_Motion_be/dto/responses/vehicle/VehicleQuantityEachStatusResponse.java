package com.swp391.e_Motion_be.dto.responses.vehicle;

import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleQuantityEachStatusResponse {
    private Long quantity;
    private VehicleStatus status;
}
