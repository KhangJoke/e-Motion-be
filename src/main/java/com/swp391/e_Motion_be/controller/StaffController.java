package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.service.StaffService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;


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
}
