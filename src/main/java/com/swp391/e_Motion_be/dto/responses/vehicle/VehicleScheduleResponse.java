package com.swp391.e_Motion_be.dto.responses.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleScheduleResponse {
    LocalDateTime startTime;
    LocalDateTime endTime;
}
