package com.swp391.e_Motion_be.dto.responses.rental;

import com.swp391.e_Motion_be.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalListResponse {
    private long id;
    private String userEmail;
    private RentalStatus status;
    LocalDateTime startTime;
    LocalDateTime endTime;
}
