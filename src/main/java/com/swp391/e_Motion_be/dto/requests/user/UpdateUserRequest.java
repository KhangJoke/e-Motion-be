package com.swp391.e_Motion_be.dto.requests.user;

import com.swp391.e_Motion_be.enums.Role;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min=10, max=255, message = "Email must be between 5 and 255 characters")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    private String email;

    private String password;

    private String fullName;

    @Pattern(regexp = "^(84|0[3|5|7|8|9])[0-9]{8}$", message = "Invalid phone number")
    private String phone;

    private Role role;
}
