package com.swp391.e_Motion_be.dto.requests;

import lombok.Data;

@Data
public class VerifyUserDto {
    private String email;
    private String verificationCode;
}
