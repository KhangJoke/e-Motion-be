package com.swp391.e_Motion_be.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentalExtendMessage {
    private Long rentalId;
    private LocalDateTime oldEndTime;
    private LocalDateTime newEndTime;
    private Double additionalFee;
    private String userEmail;
    private LocalDateTime extendedAt;
}
