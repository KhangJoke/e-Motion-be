package com.swp391.e_Motion_be.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalCheckListResponse {
    private Long id;
    private String type;
    private Double fee;
    private Double currentBattery;
    private Long rentalId;
    private String staffEmail;
    private LocalDateTime createdAt;
}
