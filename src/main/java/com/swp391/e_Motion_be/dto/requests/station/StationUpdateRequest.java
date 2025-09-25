package com.swp391.e_Motion_be.dto.requests.station;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StationUpdateRequest {
    @NotBlank(message = "Name must not be blank")
    @Size(min = 8, message = "Name must be at least 8 characters")
    private String name;

    @NotBlank(message = "Address must not be blank")
    @Size(min = 10, message = "Address must be at least 10 characters")
    private String address;

    private boolean stationStatus;
}
