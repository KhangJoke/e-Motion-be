package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserResponse {
    private String fullName;
    private String email;
    private Role role;
    private List<UserDocumentResponse> userDocuments;
}
