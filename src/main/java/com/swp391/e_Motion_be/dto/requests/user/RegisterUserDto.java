package com.swp391.e_Motion_be.dto.requests.user;

import lombok.Data;

@Data
public class RegisterUserDto {
    private String email;
    private String password;
    private String fullName;
}
