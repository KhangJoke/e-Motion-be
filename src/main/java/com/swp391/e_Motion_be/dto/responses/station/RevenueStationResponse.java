package com.swp391.e_Motion_be.dto.responses.station;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueStationResponse {
    private String stationName;
    private double revenue;
}
