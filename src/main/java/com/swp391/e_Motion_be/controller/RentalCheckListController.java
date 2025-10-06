package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.service.RentalCheckListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}

