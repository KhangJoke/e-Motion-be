package com.swp391.e_Motion_be.dto.requests.staff;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffUpdateRequest {

    @NotNull(message = "User Email is required")
    String email;

    @NotNull(message = "New Station Name is required")
    private String newStationName;

    @NotNull(message = "Old Station Name is required")
    private String oldStationName;
}
