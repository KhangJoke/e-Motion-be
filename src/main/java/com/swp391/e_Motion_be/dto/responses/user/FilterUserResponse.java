package com.swp391.e_Motion_be.dto.responses.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterUserResponse {
    private List<UserResponse> content;
    private int totalPages;
}
