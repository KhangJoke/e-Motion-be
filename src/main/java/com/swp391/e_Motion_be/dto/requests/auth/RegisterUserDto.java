package com.swp391.e_Motion_be.dto.requests.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDto {
    @NotBlank(message = "Email must not be blank")
    @Size(min=10, max=255, message = "Email must be between 5 and 255 characters")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String userPassword;

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @Pattern(regexp = "^(84|0[3|5|7|8|9])[0-9]{8}$", message = "Invalid phone number")
    private String phone;
}
