package com.swp391.e_Motion_be.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgVehicleResponse {
    private String url;
    private boolean main;
}
