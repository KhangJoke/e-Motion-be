package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.StationCity;
import com.swp391.e_Motion_be.enums.StationStatus;
import lombok.Data;

@Data
public class StationResponse {

    private String name;
    private String address;
    private String stationCity;
    private String stationStatus;
}
