package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.ImgVehicleResponse;
import com.swp391.e_Motion_be.service.ImgVehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imgVehicles")
@RequiredArgsConstructor
public class ImgVehicleController {

    private final ImgVehicleService imgVehicleService;

    // Create
//    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
//    public ApiResponse<ImgVehicleResponse> create(@RequestBody @Valid ImgVehicleCreationRequest request) {
//        ImgVehicleResponse response = imgVehicleService.create(request);
//        return new ApiResponse<>(200, "Image created successfully", response);
//    }

    // Find all
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ImgVehicleResponse>> findAll() {
        List<ImgVehicleResponse> response = imgVehicleService.findAll();
        return new ApiResponse<>(200, "Success", response);
    }

    // Find by Vehicle ID
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<ImgVehicleResponse>> findByVehicleId(@PathVariable Long vehicleId) {
        List<ImgVehicleResponse> response = imgVehicleService.findByVehicleId(vehicleId);
        return new ApiResponse<>(200, "Success", response);
    }

    // Update
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<ImgVehicleResponse> update(@PathVariable Long id,
                                                  @RequestBody @Valid ImgVehicleUpdateRequest request) {
        ImgVehicleResponse response = imgVehicleService.update(id, request);
        return new ApiResponse<>(200, "Image updated successfully", response);
    }

    // Optional: Delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<String> delete(@PathVariable Long id) {
        imgVehicleService.deleteImgVehicle(id);
        return new ApiResponse<>(200, "Image deleted successfully", null);
    }

}
