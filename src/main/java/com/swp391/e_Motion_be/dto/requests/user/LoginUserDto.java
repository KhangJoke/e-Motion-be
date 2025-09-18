package com.swp391.e_Motion_be.dto.requests.user;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
}
