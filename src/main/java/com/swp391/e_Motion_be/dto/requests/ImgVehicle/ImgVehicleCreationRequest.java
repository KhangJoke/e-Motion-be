package com.swp391.e_Motion_be.dto.requests.ImgVehicle;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgVehicleCreationRequest {
    @NotNull(message = "VehicleId is required")
    private Long vehicleId;

    @NotNull(message = "Image URL is required")
    private String url;

    @NotNull(message = "Main Image is required")
    private boolean main;
}
