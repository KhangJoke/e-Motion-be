package com.swp391.e_Motion_be.dto.requests.ImgVehicle;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgVehicleCreationRequest {
    @NotNull(message = "VehicleId cannot be null")
    private Long vehicleId;

    @NotNull(message = "Image URL cannot be null")
    private String url;
}
