package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.deposit.DepositUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {
    private final DepositService depositService;

    @GetMapping
    ApiResponse<List<DepositResponse>> getAllDeposits(){
        ApiResponse<List<DepositResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get deposit successfully");
        apiResponse.setData(depositService.getAllDeposits());
        return apiResponse;
    }

    @PostMapping
    ApiResponse<DepositResponse> createDeposit(@Valid @RequestBody DepositCreateRequest request){
        ApiResponse<DepositResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Create deposit successfully");
        apiResponse.setData(depositService.createDeposit(request));
        return apiResponse;
    }


    @PatchMapping("/status")
    ApiResponse<DepositResponse> updateDepositStatus(@RequestBody @Valid DepositUpdateRequest request){
        ApiResponse<DepositResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(depositService.updateDepositStatus(request));
        ApiResponse.setMessage("Update deposit status successfully!");
        return ApiResponse;
    }

}
