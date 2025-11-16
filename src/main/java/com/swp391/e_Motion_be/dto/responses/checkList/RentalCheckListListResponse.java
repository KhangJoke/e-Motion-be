package com.swp391.e_Motion_be.dto.responses.checkList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCheckListListResponse {
    private long rentalId;
    private String staffEmail;
    private double penaltyFee;
    private LocalDateTime createdAt;
}
