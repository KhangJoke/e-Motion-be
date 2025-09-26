package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
    private String code;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime endTime;
    private String userEmail;
    private Long vehicleId;
    private Long stationId;
}

