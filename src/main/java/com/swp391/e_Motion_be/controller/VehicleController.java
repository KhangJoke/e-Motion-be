package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.service.Vehicle.VehicleService;
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
    public ApiResponse<List<Vehicle>> getAllVehicles() {
        return new ApiResponse<>(200, "success", vehicleService.findAllVehicle());
    }

    // Create a new vehicle
    @PostMapping
    public ApiResponse<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        return new ApiResponse<>(200, "Vehicle created successfully", vehicleService.createVehicle(vehicle));
    }

    // Update vehicle by ID
    @PutMapping("/{id}")
    public ApiResponse<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return new ApiResponse<>(200, "Vehicle updated successfully", vehicleService.updateVehicle(id, vehicle));
    }

    // Delete vehicle by ID
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        return new ApiResponse<>(200, "Vehicle deleted successfully", null);
    }

    // Search vehicles by name
    @GetMapping("/search")
    public ApiResponse<List<Vehicle>> searchVehicles(@RequestParam String name) {
        return new ApiResponse<>(200, "success", vehicleService.searchVehiclesByName(name));
    }
}
