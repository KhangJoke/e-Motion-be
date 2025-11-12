package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.vehicle.*;
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

    @GetMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleUpdateResponse> getUpdateCarById(@PathVariable Long id) {
        return new ApiResponse<>(200, "success", vehicleService.getUpdateCarById(id));
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
    public ApiResponse<VehicleListResponse> createVehicle(@RequestBody @Valid VehicleCreationRequest request) {
        return new ApiResponse<>(200, "Vehicle created successfully", vehicleService.createVehicle(request));
    }

    // Update vehicle by ID
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleDetailResponse> updateVehicle(@RequestBody @Valid VehicleUpdateRequest request) {
        return new ApiResponse<>(200, "Vehicle updated successfully", vehicleService.updateVehicle(id, request));
    }

    // Delete vehicle by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        return new ApiResponse<>(200, "Vehicle deleted successfully", null);
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

    @GetMapping("/status/{stationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<VehicleQuantityEachStatusResponse>> getVehicleQuantityEachStatusOfStation(@PathVariable Long stationId){
        ApiResponse<List<VehicleQuantityEachStatusResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.getVehicleQuantityEachStatusOfStation(stationId));
        response.setMessage("Get vehicle of station successfully");
        return response;
    }


    @PostMapping("/filter/available")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PageAndFilterVehicleResponse>> getAvailableVehicles(@RequestBody PageAndFilterVehicleRequest request){
        ApiResponse<PageAndFilterVehicleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(vehicleService.findAvailableVehicles(request));
        apiResponse.setMessage("Get vehicles successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/filter/unavailable")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PageAndFilterVehicleResponse>> getUnavailableVehicles(@RequestBody PageAndFilterVehicleRequest request){
        ApiResponse<PageAndFilterVehicleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(vehicleService.findUnavailableVehicles(request));
        apiResponse.setMessage("Get vehicles successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/manage")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageAndFilterVehicleResponse>> manageCar(@RequestBody PageAndFilterManageVehicleRequest request){
        ApiResponse<PageAndFilterVehicleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(vehicleService.manageCar(request));
        apiResponse.setMessage("Get vehicles successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}/schedule")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<VehicleScheduleResponse>> getVehicleFullSchedule(@PathVariable Long id){
        ApiResponse<List<VehicleScheduleResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.getVehicleFullSchedule(id));
        response.setMessage("Get vehicle schedule successfully");
        return response;
    }
}
