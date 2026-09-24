package com.swp391.e_Motion_be.dto.requests.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
}
