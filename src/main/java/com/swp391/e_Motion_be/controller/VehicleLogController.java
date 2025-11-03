package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.vehicleLog.PageAndFilterVehicleLogRequest;
import com.swp391.e_Motion_be.dto.requests.vehicleLog.VehicleLogCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicleLog.VehicleLogUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.vehicleLog.PageAndFilterVehicleLogResponse;
import com.swp391.e_Motion_be.dto.responses.vehicleLog.VehicleLogResponse;
import com.swp391.e_Motion_be.service.VehicleLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicleLogs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
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
    @GetMapping("/user/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<VehicleLogResponse>> getVehicleLogsByUser(@PathVariable Long id) {
        List<VehicleLogResponse> response = vehicleLogService.findVehicleLogByStaffId(id);
        return new ApiResponse<>(200, "Success", response);
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageAndFilterVehicleLogResponse>> findByPageAndFilterAndSearch(@RequestBody PageAndFilterVehicleLogRequest request){
        ApiResponse<PageAndFilterVehicleLogResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(vehicleLogService.findByPageAndFilterAndSearch(request));
        if(apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No rentals found");
        }else {
            apiResponse.setMessage("Get rentals successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }
}

