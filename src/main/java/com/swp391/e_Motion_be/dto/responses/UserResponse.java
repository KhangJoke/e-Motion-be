package com.swp391.e_Motion_be.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swp391.e_Motion_be.entity.UserDocument;
import com.swp391.e_Motion_be.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserResponse {
    private String fullName;
    private String email;
    private Role role;
    private List<UserDocumentRespon> userDocuments;
}
