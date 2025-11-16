package com.swp391.e_Motion_be.dto.responses.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeakHourResponse {
    private int hour;
    private long traffic;
}
