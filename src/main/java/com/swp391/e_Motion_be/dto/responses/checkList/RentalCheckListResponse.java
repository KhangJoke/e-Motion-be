package com.swp391.e_Motion_be.dto.responses.checkList;

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
    private String img;
    private Long rentalId;
    private String staffEmail;
    private LocalDateTime createdAt;
}
