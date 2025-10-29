package com.swp391.e_Motion_be.dto.responses.user;

import com.swp391.e_Motion_be.dto.responses.DocumentResponse;
import com.swp391.e_Motion_be.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserResponse {
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private LocalDate createdAt;
    private boolean blocked;
    private List<DocumentResponse> documents;
    private Long staffId;
    private String stationName;
}
