package com.swp391.e_Motion_be.dto.responses.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffStatsResponse {
    private int deliveries;
    private int pickups;
}
