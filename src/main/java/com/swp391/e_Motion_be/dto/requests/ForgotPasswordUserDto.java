package com.swp391.e_Motion_be.dto.requests;

import lombok.Data;

@Data
public class ForgotPasswordUserDto {
    private String email;
    private String newPassword;
    private String confirmNewPassword;
    private String verificationCode;
}
