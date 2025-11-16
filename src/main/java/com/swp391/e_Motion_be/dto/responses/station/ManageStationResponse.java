package com.swp391.e_Motion_be.dto.responses.station;

import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.station.StationStatus;
import lombok.Data;

@Data
public class ManageStationResponse {
    private Long id;
    private String name;
    private String address;
    private StationCity city;
    private StationStatus status;
    private Long quantityCar;
    private Long quantityStaff;
}
