package com.swp391.e_Motion_be.dto.responses;

import lombok.Data;

@Data
public class StationResponse {

    private String name;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String stationStatus;
}
