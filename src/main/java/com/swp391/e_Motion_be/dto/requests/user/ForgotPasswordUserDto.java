package com.swp391.e_Motion_be.dto.requests.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordUserDto {
    @NotBlank(message = "Email must not be blank")
    @Size(min=10, max=255, message = "Email must be between 5 and 255 characters")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "New password must not be blank")
    private String newPassword;

}
