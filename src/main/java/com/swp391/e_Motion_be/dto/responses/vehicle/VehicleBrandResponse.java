package com.swp391.e_Motion_be.dto.responses.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleBrandResponse {
    private String brand;
    private Long brandId;
    private String brandLogo;
}
