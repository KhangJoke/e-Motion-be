package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.station.StationCity;
import lombok.Data;

@Data
public class StationResponse {

    private String name;
    private String address;
    private StationCity city;
    private Double latitude;
    private Double longitude;
    private String status;
}
