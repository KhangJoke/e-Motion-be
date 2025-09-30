package com.swp391.e_Motion_be.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingResponse {
    private String comment;
    private int score;
    private Long userId;
    private Long rentalId;
}
