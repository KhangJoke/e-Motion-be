package com.swp391.e_Motion_be.dto.requests.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Full name must not be blank")
    private String fullName;
    @Pattern(regexp = "^(0(3|5|7|8|9)[0-9]{8})$", message = "Invalid phone number")
    private String phone;
}
