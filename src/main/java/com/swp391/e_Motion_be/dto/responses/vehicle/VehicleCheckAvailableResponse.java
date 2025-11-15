package com.swp391.e_Motion_be.dto.responses.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCheckAvailableResponse {
    private Boolean available;
}
