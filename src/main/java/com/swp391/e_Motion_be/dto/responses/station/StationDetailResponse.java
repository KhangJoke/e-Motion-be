package com.swp391.e_Motion_be.dto.responses.station;

import com.swp391.e_Motion_be.dto.responses.rental.RentalListResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.station.StationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StationDetailResponse {
    private Long id;
    private String name;
    private String address;
    private StationCity city;
    private StationStatus status;
    private Long quantityCar;
    private Long quantityStaff;
    private LocalDate createdAt;
    private Long carRental;
    private List<Integer> peakHours;
    private List<RentalListResponse> rentals;
}
