package com.swp391.e_Motion_be.dto.responses.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataAdminDashboard {
    private TotalStatsResponse totalStats;
    private List<StationStatsResponse> stationStats;
    private List<RevenueResponse> revenueInYear;
    private List<PeakHourResponse> peakHours;
}
