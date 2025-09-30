package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.StationResponse;
import com.swp391.e_Motion_be.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping
    public ApiResponse<StationResponse> createStations(@RequestBody StationCreationRequest request) {
        ApiResponse<StationResponse> response = new ApiResponse<>();
        response.setData(stationService.createStation(request));
        response.setMessage("Create station successfully");
        return response;
    }

    @GetMapping
   public ApiResponse<List<StationResponse>> getAllStations() {
        ApiResponse<List<StationResponse>> response = new ApiResponse<>();
        response.setData(stationService.findAllStations());
        response.setMessage("Get all stations successfully");
        return response;
    }

    @GetMapping("/name/{name}")
    public ApiResponse<StationResponse> getStationByName(@PathVariable String name) {
        ApiResponse<StationResponse> response = new ApiResponse<>();
        response.setData(stationService.getStationByName(name));
        response.setMessage("Get station by name successfully");
        return response;
    }

    @GetMapping("/address/{address}")
    public ApiResponse<List<StationResponse>> getStationsByAddress(@PathVariable String address) {
        ApiResponse<List<StationResponse>> response = new ApiResponse<>();
        response.setData(stationService.getStationsByAddress(address));
        response.setMessage("Get stations by address successfully");
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

}
