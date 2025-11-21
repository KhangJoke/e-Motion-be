package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.staff.DispatchStaffRequest;
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
public class StaffController {
    private final StaffService staffService;


    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<StaffResponse> getStaffByUserEmail(@PathVariable String email) {
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.getStaffByUserEmail(email));
        response.setMessage("Get staff by user email successfully");
        return response;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<StaffResponse>> getAllStaffs() {
        ApiResponse<List<StaffResponse>> response = new ApiResponse<>();
        response.setData(staffService.getAllStaffs());
        response.setMessage("Get all staffs successfully");
        return response;
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteStaff(@PathVariable String email){
        staffService.deleteStaff(email);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Delete staff by email successfully");
        return response;
    }

    @PostMapping("/dispatch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> assignStaffToStation(@RequestBody @Valid DispatchStaffRequest request){
        staffService.dispatchStaffs(request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Assign staff to station successfully");
        return response;
    }
}
