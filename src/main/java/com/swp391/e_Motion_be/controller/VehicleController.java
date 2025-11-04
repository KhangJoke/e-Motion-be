package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.vehicle.PageAndFilterVehicleRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.*;
import com.swp391.e_Motion_be.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // Find by ID
    @GetMapping("/id/{id}")
    @PreAuthorize("permitAll()")
    public ApiResponse<VehicleDetailResponse> findById(@PathVariable Long id) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleById(id));
    }

    // Find by PlateNumber
    @GetMapping("/plate/{plateNumber}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<VehicleDetailResponse> findByPlateNumber(@PathVariable String plateNumber) {
        return new ApiResponse<>(200, "success", vehicleService.findVehicleByPlateNumber(plateNumber));
    }

    // Find all available
    @GetMapping
    @PreAuthorize("permitAll()")
    public ApiResponse<List<VehicleListResponse>> getAllVehicles() {
        ApiResponse<List<VehicleListResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.findAllVehicles());
        return response;
    }

    // Find by brand
    @GetMapping("/brand/{brand}")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<VehicleListResponse>> getVehiclesByBrand(@PathVariable String brand) {
        ApiResponse<List<VehicleListResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.findVehicleByBrand(brand));
        return response;
    }

    // Create a new vehicle
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleDetailResponse> createVehicle(@RequestBody @Valid VehicleCreationRequest request) {
        return new ApiResponse<>(200, "Vehicle created successfully", vehicleService.createVehicle(request));
    }

    // Update vehicle by ID
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleDetailResponse> updateVehicle(@PathVariable Long id, @RequestBody @Valid VehicleUpdateRequest request) {
        return new ApiResponse<>(200, "Vehicle updated successfully", vehicleService.updateVehicle(id, request));
    }

    // Delete vehicle by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        return new ApiResponse<>(200, "Vehicle deleted successfully", null);
    }

    // Search bằng thanh tìm kiếm
    @PostMapping("/search")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<VehicleListResponse>> searchVehicles(@RequestBody @Valid VehicleFindRequest request){
        ApiResponse<List<VehicleListResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.searchVehicles(request));
        return response;
    }

    // Get ra danh sách đang thuê và đặt trước của xe
    @GetMapping("/schedule/{vid}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<VehicleScheduleResponse>> scheduleVehicles(@PathVariable Long vid){
        ApiResponse<List<VehicleScheduleResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.getVehicleSchedule(vid));
        return response;
    }

    @GetMapping("/booking")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<FeeResponse>> getListFeeBooking(@RequestParam("id") Long vid,
                                                            @RequestParam("startTime") String start,
                                                            @RequestParam("endTime") String end){
        ApiResponse<List<FeeResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.getListFeeBooking(vid, start, end));
        return response;
    }

    @PostMapping("/filter")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PageAndFilterVehicleResponse>> findByPageAndFilterAndSearch(@RequestBody PageAndFilterVehicleRequest request){
        ApiResponse<PageAndFilterVehicleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(vehicleService.findByPageAndFilterAndSearch(request));
        if(apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No vehicle found");
        }else {
            apiResponse.setMessage("Get vehicles successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }
}
