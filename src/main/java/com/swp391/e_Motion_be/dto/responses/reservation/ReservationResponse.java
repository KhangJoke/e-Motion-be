package com.swp391.e_Motion_be.dto.responses.reservation;

import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
    private String code;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime endTime;
    private Boolean overdueNotified;
    private Boolean expiringNotified;
    private String userEmail;
    private VehicleDetailResponse vehicle;
    private Long stationId;
}

