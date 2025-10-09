package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleSearchResponse;
import com.swp391.e_Motion_be.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // Find by ID
    @GetMapping("/id/{id}")
    public ApiResponse<VehicleDetailResponse> findById(@PathVariable Long id) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleById(id));
    }

    // Find by PlateNumber
    @GetMapping("/plate/{plateNumber}")
    public ApiResponse<VehicleDetailResponse> findByPlateNumber(@PathVariable String plateNumber) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleByPlateNumber(plateNumber));
    }

    // Find all available
    @GetMapping("/vehicles")
    public ApiResponse<List<VehicleListResponse>> getAllVehicles() {
        ApiResponse<List<VehicleListResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.findAllVehicles());
        return response;
    }

    // Find by brand
    @GetMapping("/vehicles/brand/{brand}")
    public ApiResponse<List<VehicleListResponse>> getVehiclesByBrand(@PathVariable String brand) {
        ApiResponse<List<VehicleListResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.findVehicleByBrand(brand));
        return response;
    }

    // Create a new vehicle
    @PostMapping
    public ApiResponse<VehicleDetailResponse> createVehicle(@RequestBody @Valid VehicleCreationRequest request) {
        return new ApiResponse<>(200, "Vehicle created successfully", vehicleService.createVehicle(request));
    }

    // Update vehicle by ID
    @PutMapping("/{id}")
    public ApiResponse<VehicleDetailResponse> updateVehicle(@PathVariable Long id, @RequestBody @Valid VehicleUpdateRequest request) {
        return new ApiResponse<>(200, "Vehicle updated successfully", vehicleService.updateVehicle(id, request));
    }

    // Delete vehicle by ID
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        return new ApiResponse<>(200, "Vehicle deleted successfully", null);
    }

    // Search bằng thanh tìm kiếm
    @PostMapping("/search")
    public ApiResponse<VehicleSearchResponse> searchVehicles(@RequestBody @Valid VehicleFindRequest request){
        ApiResponse<VehicleSearchResponse> response = new ApiResponse<>();
        response.setData(vehicleService.searchVehicles(request));
        return response;
    }
}
