package com.swp391.e_Motion_be.dto.responses;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgVehicleResponse {
    private Long vehicleId;
    private String url;
    private String publicId;
}
