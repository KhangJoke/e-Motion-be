package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterManageVehicleRequest {
    private List<VehicleStatus> status;
    private Integer page;
    private Integer limit;
    private String search;
    private Long stationId;
}
