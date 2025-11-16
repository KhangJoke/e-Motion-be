package com.swp391.e_Motion_be.dto.requests.vehicleLog;

import lombok.Data;

@Data
public class PageAndFilterVehicleLogRequest {
    private Integer page;
    private Integer limit;
    private Long search;
}
