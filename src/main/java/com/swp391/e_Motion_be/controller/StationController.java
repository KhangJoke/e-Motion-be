package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.dto.responses.station.ManageStationResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationDetailResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationResponse;
import com.swp391.e_Motion_be.dto.responses.stats.RevenueResponse;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StationResponse> createStations(@RequestBody StationCreationRequest request) {
        ApiResponse<StationResponse> response = new ApiResponse<>();
        response.setData(stationService.createStation(request));
        response.setMessage("Create station successfully");
        return response;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StationResponse>> getAllStations() {
        ApiResponse<List<StationResponse>> response = new ApiResponse<>();
        response.setData(stationService.findAllStations());
        response.setMessage("Get all stations successfully");
        return response;
    }

    @GetMapping("/cities")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> getAllCity() {
        ApiResponse<List<String>> response = new ApiResponse<>();
        response.setData(stationService.findAllCity());
        response.setMessage("Get all city successfully");
        return response;
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StationResponse> getStationByName(@PathVariable String name) {
        ApiResponse<StationResponse> response = new ApiResponse<>();
        response.setData(stationService.getStationByName(name));
        response.setMessage("Get station by name successfully");
        return response;
    }

    @GetMapping("/{stationId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StationDetailResponse> getStationById(@PathVariable Long stationId) {
        ApiResponse<StationDetailResponse> response = new ApiResponse<>();
        response.setData(stationService.getStationById(stationId));
        response.setMessage("Get station by ID successfully");
        return response;
    }

    @GetMapping("/address/{address}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StationResponse>> getStationsByAddress(@PathVariable String address) {
        ApiResponse<List<StationResponse>> response = new ApiResponse<>();
        response.setData(stationService.getStationsByAddress(address));
        response.setMessage("Get stations by address successfully");
        return response;
    }

    @GetMapping("/city/{city}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StationResponse>> getStationsByCity(@PathVariable("city") StationCity stationCity) {
        ApiResponse<List<StationResponse>> response = new ApiResponse<>();
        response.setData(stationService.getStationsByCity(stationCity));
        response.setMessage("Get stations by city successfully");
        return response;
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StationResponse> updateStation(@PathVariable String name,
                                                      @RequestBody StationUpdateRequest request) {
        ApiResponse<StationResponse> response = new ApiResponse<>();
        response.setData(stationService.updateStation(name, request));
        response.setMessage("Update station successfully");
        return response;
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteStation(@PathVariable String name) {
        stationService.deleteStation(name);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Delete station successfully");
        return response;
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ManageStationResponse>>> getDataManageStation(){
        ApiResponse<List<ManageStationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get data manage station successfully");
        apiResponse.setData(stationService.getDataManageStation());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RevenueResponse>>> getRevenueAllStation(
            @RequestParam(defaultValue = "month") String type,
            @RequestParam Integer day,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        ApiResponse<List<RevenueResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setData(stationService.getRevenueStation(type, day, month, year));
        apiResponse.setMessage("Get station revenue successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/revenue/{stationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<RevenueResponse>>> getRevenueEachStation(@PathVariable Long stationId) {
        ApiResponse<List<RevenueResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setData(stationService.getWeeklyRevenueOfStation(stationId));
        apiResponse.setMessage("Get station revenue successfully");
        return ResponseEntity.ok(apiResponse);
    }

}
