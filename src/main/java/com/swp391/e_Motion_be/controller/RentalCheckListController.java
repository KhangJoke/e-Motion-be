package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import com.swp391.e_Motion_be.service.RentalCheckListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-checklists")
@RequiredArgsConstructor
public class RentalCheckListController {

    private final RentalCheckListService rentalCheckListService;

    @PostMapping
    public ApiResponse<RentalCheckListResponse> createCheckList(
            @RequestBody @Valid RentalCheckListCreateRequest request) {
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckList(request));
        return response;
    }

    @GetMapping
    public ApiResponse<List<RentalCheckListResponse>> getAllCheckList() {
        ApiResponse<List<RentalCheckListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getAllCheckLists());
        response.setMessage("Get all rental checklists successfully");
        return response;
    }
}

