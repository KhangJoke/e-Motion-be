package com.swp391.e_Motion_be.dto.responses.rental;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalResponse {
    long id;
    String status;
    LocalDateTime startTime;
    LocalDateTime endTime;
    double rentFee;
    LocalDateTime createdAt;
    long vehicleId;
    Long reservationId;
    long userId;
    long stationId;
    long staffId;
}
