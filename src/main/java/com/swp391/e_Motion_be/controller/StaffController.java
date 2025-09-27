package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.staff.StaffCreationRequest;
import com.swp391.e_Motion_be.dto.requests.staff.StaffUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.service.staff.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;

    @PostMapping
    public ApiResponse<StaffResponse> createStaff(@RequestBody StaffCreationRequest request){
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.createStaff(request));
        response.setMessage("Create staff successfully");
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<StaffResponse> getStaffById(@PathVariable Long id) {
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.getStaffById(id));
        response.setMessage("Get staff by id successfully");
        return response;
    }

    @GetMapping
    public ApiResponse<List<StaffResponse>> getAllStaffs() {
        ApiResponse<List<StaffResponse>> response = new ApiResponse<>();
        response.setData(staffService.getAllStaffs());
        response.setMessage("Get all staffs successfully");
        return response;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStaffById(@PathVariable Long id){
        staffService.deleteStaffById(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Delete staff by id successfully");
        return response;
    }

    @PutMapping("/{id}")
    public ApiResponse<StaffResponse> updateStaffById(@PathVariable Long id,
                                                      @RequestBody StaffUpdateRequest request){
        ApiResponse<StaffResponse> response = new ApiResponse<>();
        response.setData(staffService.updateStaffById(id ,request));
        response.setMessage("Update staff by id successfully");
        return response;
    }
}
