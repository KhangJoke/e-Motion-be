package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.rentalCheckList.RentalCheckInResponse;
import com.swp391.e_Motion_be.dto.responses.rentalCheckList.RentalCheckOutResponse;
import com.swp391.e_Motion_be.service.RentalCheckListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-checklists")
@RequiredArgsConstructor
public class RentalCheckListController {
    private final RentalCheckListService rentalCheckListService;

    @GetMapping
    public ApiResponse<List<RentalCheckOutResponse>> getAllCheckLists(){
        ApiResponse<List<RentalCheckOutResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getAllCheckLists());
        response.setMessage("Get all rental checklists successfully");
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<RentalCheckOutResponse> getCheckListById(@PathVariable Long id) {
        ApiResponse<RentalCheckOutResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListById(id));
        response.setMessage("Get rental checklist by id successfully");
        return response;
    }

    @GetMapping("/rental/{rentalId}")
    public ApiResponse<List<RentalCheckOutResponse>> getCheckListsByRental(@PathVariable Long rentalId) {
        ApiResponse<List<RentalCheckOutResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListsByRental(rentalId));
        response.setMessage("Get rental checklists by rental id successfully");
        return response;
    }

    @GetMapping("/staff/{email}")
    public ApiResponse<List<RentalCheckOutResponse>> getCheckListsByStaff(@PathVariable String email) {
        ApiResponse<List<RentalCheckOutResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListsByStaffEmail(email));
        response.setMessage("Get rental checklists by staff email successfully");
        return response;
    }

    @PostMapping
    public ApiResponse<RentalCheckOutResponse> createCheckList(@RequestBody RentalCheckListCreateRequest request, @RequestParam String staffEmail ) {
        ApiResponse<RentalCheckOutResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckList(request, staffEmail));
        response.setMessage("Create rental checklist successfully");
        return response;
    }

    @PutMapping
    public ApiResponse<RentalCheckOutResponse> updateCheckList(@RequestBody RentalCheckListUpdateRequest request, @RequestParam String staffEmail ) {
        ApiResponse<RentalCheckOutResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.updateCheckList(request, staffEmail));
        response.setMessage("Update rental checklist successfully");
        return response;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCheckList( @PathVariable Long id, @RequestParam String staffEmail ) {
        ApiResponse<String> response = new ApiResponse<>();
        rentalCheckListService.deleteCheckList(id, staffEmail);
        response.setMessage("Delete rental checklist successfully");
        return response;
    }

    @PostMapping("/checkin")
    public ApiResponse<RentalCheckInResponse> checkIn(
            @RequestBody RentalCheckListCreateRequest request) {

        ApiResponse<RentalCheckInResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckIn(request));
        response.setMessage("Check-in successfully");
        return response;
    }

    @PostMapping("/checkout")
    public ApiResponse<RentalCheckOutResponse> checkOut(
            @RequestBody RentalCheckListCreateRequest request) {

        ApiResponse<RentalCheckOutResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckOut(request));
        response.setMessage("Check-out successfully and fee calculated");
        return response;
    }
}

