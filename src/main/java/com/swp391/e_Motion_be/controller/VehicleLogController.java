package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.VehicleLog.VehicleLogCreationRequest;
import com.swp391.e_Motion_be.dto.requests.VehicleLog.VehicleLogUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.VehicleLogResponse;
import com.swp391.e_Motion_be.service.vehicleLog.VehicleLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicleLogs")
@RequiredArgsConstructor
public class VehicleLogController {

    private final VehicleLogService vehicleLogService;

    // Create a new vehicle log
    @PostMapping
    public ApiResponse<VehicleLogResponse> createVehicleLog(
            @RequestBody @Valid VehicleLogCreationRequest request) {
        VehicleLogResponse response = vehicleLogService.createVehicleLog(request);
        return new ApiResponse<>(200, "Vehicle log created successfully", response);
    }

    // Get a single vehicle log by logId
    @GetMapping("/{id}")
    public ApiResponse<VehicleLogResponse> getVehicleLog(@PathVariable Long id) {
        VehicleLogResponse response = vehicleLogService.findVehicleLogById(id);
        return new ApiResponse<>(200, "Success", response);
    }

    // Get all vehicle logs
    @GetMapping
    public ApiResponse<List<VehicleLogResponse>> getAllVehicleLogs() {
        List<VehicleLogResponse> response = vehicleLogService.findAllVehicleLogs();
        return new ApiResponse<>(200, "Success", response);
    }

    // Update a vehicle log
    @PutMapping("/{id}")
    public ApiResponse<VehicleLogResponse> updateVehicleLog(
            @PathVariable Long id,
            @RequestBody @Valid VehicleLogUpdateRequest request) {
        VehicleLogResponse response = vehicleLogService.updateVehicleLog(id, request);
        return new ApiResponse<>(200, "Vehicle log updated successfully", response);
    }

    // Delete a vehicle log
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVehicleLog(@PathVariable Long id) {
        vehicleLogService.deleteVehicleLog(id);
        return new ApiResponse<>(200, "Vehicle log deleted successfully", null);
    }

    // Find logs by vehicleId
    @GetMapping("/vehicle/{vehicleId}")
    public ApiResponse<List<VehicleLogResponse>> getVehicleLogsByVehicle(@PathVariable Long vehicleId) {
        List<VehicleLogResponse> response = vehicleLogService.findVehicleLogByVehicleId(vehicleId);
        return new ApiResponse<>(200, "Success", response);
    }

    // Find logs by userId
    @GetMapping("/user/{userId}")
    public ApiResponse<List<VehicleLogResponse>> getVehicleLogsByUser(@PathVariable Long userId) {
        List<VehicleLogResponse> response = vehicleLogService.findVehicleLogByStaffId(userId);
        return new ApiResponse<>(200, "Success", response);
    }
}

