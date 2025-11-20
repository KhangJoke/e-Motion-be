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

    // Find 16 vehicles for home page
    @GetMapping("/home")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<VehicleListResponse>> getVehiclesForHomePage() {
        ApiResponse<List<VehicleListResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.findVehiclesForHomePage());
        response.setMessage("Get " +response.getData().size()+  " vehicles for home page successfully");
        return response;
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
        response.setMessage("Get " +response.getData().size()+  " vehicles by brand successfully");
        return response;
    }

    @GetMapping("/brand")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<String>> findAllVehicleBrands() {
        ApiResponse<List<String>> response = new ApiResponse<>();
        response.setData(vehicleService.findAllVehicleBrands());
        return response;
    }

    @GetMapping("/category")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<String>> findAllVehicleCategories() {
        ApiResponse<List<String>> response = new ApiResponse<>();
        response.setData(vehicleService.findAllVehicleCategory());
        return response;
    }


    @GetMapping("/check-available")
    @PreAuthorize("permitAll()")
    public ApiResponse<VehicleCheckAvailableResponse> vehicleCheckAvailable(@RequestBody @Valid VehicleCheckAvailableRequest request) {
        ApiResponse<VehicleCheckAvailableResponse> response = new ApiResponse<>();
        response.setData(vehicleService.vehicleCheckAvailable(request));
        return response;
    }

    // Create a new vehicle
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleListResponse> createVehicle(@RequestBody @Valid VehicleCreationRequest request) {
        ApiResponse<VehicleListResponse> response = new ApiResponse<>();
        response.setData(vehicleService.createVehicle(request));
        response.setMessage("Vehicle created successfully");
        return response;
    }

    // Update vehicle by ID
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> updateVehicle(@RequestBody @Valid VehicleUpdateRequest request) {
        ApiResponse<String> response = new ApiResponse<>();
        vehicleService.updateVehicle(request);
        response.setMessage("Vehicle updated successfully");
        return response;
    }

    // Delete vehicle by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Vehicle deleted successfully");
        return response;
    }

    @PostMapping("/booking")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<FeeResponse>> getListFeeBooking(@RequestBody @Valid FeeRequest request){
        ApiResponse<List<FeeResponse>> response = new ApiResponse<>();
        response.setData(vehicleService.getListFeeBooking(request));
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

    @PostMapping("/dispatch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> dispatchVehicle(@RequestBody @Valid VehicleDispatchRequest request){
        vehicleService.dispatchVehicle(request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Dispatch vehicle successfully");
        return response;
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateVehicleStatus(
            @RequestBody VehicleStatusUpdateRequest request) {
        vehicleService.updateVehicleStatus(request);
        return new ApiResponse<>(200, "Vehicle status updated successfully", null);
    }

}
