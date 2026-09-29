package com.swp391.e_Motion_be.dto.requests.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginDto {
    @NotBlank(message = "ID Token must not be blank")
    private String idToken;
}
