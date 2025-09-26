package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.VehicleResponse;
import com.swp391.e_Motion_be.enums.VehicleStatus;
import com.swp391.e_Motion_be.service.Vehicle.VehicleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // Get all vehicles
    @GetMapping
    public ApiResponse<List<VehicleResponse>> getAllVehicles() {
        return new ApiResponse<>(200, "success", vehicleService.findAllVehicle());
    }

    // Find by ID
    @GetMapping("/{id}")
    public ApiResponse<VehicleResponse> findById(@PathVariable Long id) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleById(id));
    }

    // Find by PlateNumber
    @GetMapping("/{plateNumber}")
    public ApiResponse<VehicleResponse> findByPlateNumber(@PathVariable String plateNumber) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleByPlateNumber(plateNumber));
    }

    // Find by status
    @GetMapping("/status/{status}")
    public ApiResponse<List<VehicleResponse>> findByStatus(@PathVariable VehicleStatus status) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleByStatus(status));
    }

    // Create a new vehicle
    @PostMapping
    public ApiResponse<VehicleResponse> createVehicle(@RequestBody @Valid VehicleCreationRequest request) {
        return new ApiResponse<>(200, "Vehicle created successfully", vehicleService.createVehicle(request));
    }

    // Update vehicle by ID
    @PutMapping("/{id}")
    public ApiResponse<VehicleResponse> updateVehicle(@PathVariable Long id, @RequestBody @Valid VehicleUpdateRequest request) {
        return new ApiResponse<>(200, "Vehicle updated successfully", vehicleService.updateVehicle(id, request));
    }

    // Delete vehicle by ID
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        return new ApiResponse<>(200, "Vehicle deleted successfully", null);
    }

    // Search vehicles by name
    @GetMapping("/search")
    public ApiResponse<List<VehicleResponse>> searchVehicles(@RequestParam String name) {
        return new ApiResponse<>(200, "success", vehicleService.searchVehiclesByName(name));
    }


}
