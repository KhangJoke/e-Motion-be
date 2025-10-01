package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.enums.StationCity;
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
public class VehicleFindRequest {
    StationCity city;
    LocalDateTime startTime;
    LocalDateTime endTime;
}
