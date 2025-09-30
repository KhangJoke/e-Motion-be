package com.swp391.e_Motion_be.dto.requests.staff;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffCreationRequest {

    @NotNull(message = "User Email is required")
    private String email;

    @NotNull(message = "Station Name is required")
    private String stationName;
}
