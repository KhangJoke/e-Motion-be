package com.swp391.e_Motion_be.dto.requests.staff;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffCreationRequest {
    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Station ID must not be null")
    private Long stationId;
}
