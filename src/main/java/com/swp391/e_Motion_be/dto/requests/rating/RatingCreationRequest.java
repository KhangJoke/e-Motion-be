package com.swp391.e_Motion_be.dto.requests.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingCreationRequest {

    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot exceed 5")
    private int score;

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotNull(message = "RentalId is required")
    private Long rentalId;
}
