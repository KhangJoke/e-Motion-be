package com.swp391.e_Motion_be.dto.responses.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueInYearResponse {
    private int month;
    private double revenue;
}
