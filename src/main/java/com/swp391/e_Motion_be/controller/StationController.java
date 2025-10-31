package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.station.ManageStationResponse;
import com.swp391.e_Motion_be.dto.responses.station.RevenueStationResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationResponse;
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
@PreAuthorize("hasRole('ADMIN')")
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
    public ApiResponse<StationResponse> updateStation(@PathVariable String name,
                                                      @RequestBody StationUpdateRequest request) {
        ApiResponse<StationResponse> response = new ApiResponse<>();
        response.setData(stationService.updateStation(name, request));
        response.setMessage("Update station successfully");
        return response;
    }

    @DeleteMapping("/{name}")
    public ApiResponse<String> deleteStation(@PathVariable String name) {
        stationService.deleteStation(name);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Delete station successfully");
        return response;
    }

    @GetMapping("/manage")
    public ResponseEntity<ApiResponse<List<ManageStationResponse>>> getDataManageStation(){
        ApiResponse<List<ManageStationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get data manage station successfully");
        apiResponse.setData(stationService.getDataManageStation());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<RevenueStationResponse>>> getRevenueStation(
            @RequestParam(defaultValue = "month") String type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        ApiResponse<List<RevenueStationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setData(stationService.getRevenueStation(type, month, year));
        apiResponse.setMessage("Get station revenue successfully");
        return ResponseEntity.ok(apiResponse);
    }

}
