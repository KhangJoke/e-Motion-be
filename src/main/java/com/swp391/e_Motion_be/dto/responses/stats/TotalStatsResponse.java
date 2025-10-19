package com.swp391.e_Motion_be.dto.responses.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalStatsResponse {
    long totalUsers;
    long totalCars;
    long totalReservations;
    long totalBookings;
    double totalRevenue;
    double usageRate;
    List<Integer> peakHours;
}
