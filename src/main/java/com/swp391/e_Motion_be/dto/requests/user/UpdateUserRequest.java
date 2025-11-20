package com.swp391.e_Motion_be.dto.requests.user;

import com.swp391.e_Motion_be.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Thiếu email")
    private String email;
    @NotNull(message = "Thiếu role")
    private Role role;

    private Long stationId;
}
