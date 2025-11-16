package com.swp391.e_Motion_be.dto.requests.vehicle;

import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterVehicleRequest {
    private List<VehicleBrand> brands;
    private List<VehicleCategory> categories;
    private Integer page;
    private Integer limit;
    private String search;
    private Long stationId;
    private Double minPrice;
    private Double maxPrice;
    private Integer seats;
    @NotNull(message = "City must not be null")
    StationCity city;
    @NotNull(message = "Start time must not be null")
    @Future(message = "Start time must be in the future")
    LocalDateTime startTime;
    @NotNull(message = "End time must not be null")
    @Future(message = "End time must be in the future")
    LocalDateTime endTime;
}
