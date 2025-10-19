package com.swp391.e_Motion_be.dto.responses.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationStatsResponse {
    String stationName;
    double revenue;
    long totalBookings;
    double usageRate;
    List<Integer> peakHours;
}
