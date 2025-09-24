package com.swp391.e_Motion_be.dto.requests.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotBlank(message = "Token must not be blank")
    private String token;
}
