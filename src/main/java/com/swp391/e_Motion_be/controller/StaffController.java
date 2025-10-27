package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.staff.StaffCreationRequest;
import com.swp391.e_Motion_be.dto.requests.staff.StaffUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {
    private final StaffService staffService;

    @PostMapping
    public ApiResponse<StaffResponse> createStaff(@RequestBody @Valid StaffCreationRequest request){
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.createStaff(request));
        response.setMessage("Create staff successfully");
        return response;
    }

    @GetMapping("/{email}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StaffResponse> getStaffByUserEmail(@PathVariable String email) {
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.getStaffByUserEmail(email));
        response.setMessage("Get staff by user email successfully");
        return response;
    }

    @GetMapping
    public ApiResponse<List<StaffResponse>> getAllStaffs() {
        ApiResponse<List<StaffResponse>> response = new ApiResponse<>();
        response.setData(staffService.getAllStaffs());
        response.setMessage("Get all staffs successfully");
        return response;
    }

    @DeleteMapping("/{email}")
    public ApiResponse<Void> deleteStaff(@PathVariable String email){
        staffService.deleteStaff(email);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Delete staff by email successfully");
        return response;
    }

    @PutMapping("/{email}")
    public ApiResponse<StaffResponse> updateStaffByEmail(@PathVariable String email,
                                                      @RequestBody @Valid StaffUpdateRequest request){
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.updateStaff(request));
        response.setMessage("Update staff by id successfully");
        return response;
    }
}
