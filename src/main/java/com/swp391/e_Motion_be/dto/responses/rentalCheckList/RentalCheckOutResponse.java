package com.swp391.e_Motion_be.dto.responses.rentalCheckList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalCheckOutResponse {
    private Long id;
    private String type;
    private double fee;
    private double currentBattery;
    private Long rentalId;
    private String staffEmail;
    private LocalDateTime createdAt;
}
