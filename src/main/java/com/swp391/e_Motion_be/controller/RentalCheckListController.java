package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.service.rentalCheckList.RentalCheckListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-checklists")
@RequiredArgsConstructor
public class RentalCheckListController {
    private final RentalCheckListService rentalCheckListService;

    @GetMapping
    public ApiResponse<List<RentalCheckListResponse>> getAllCheckLists(){
        ApiResponse<List<RentalCheckListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getAllCheckLists());
        response.setMessage("Get all rental checklists successfully");
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<RentalCheckListResponse> getCheckListById(@PathVariable Long id) {
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListById(id));
        response.setMessage("Get rental checklist by id successfully");
        return response;
    }

    @GetMapping("/rental/{rentalId}")
    public ApiResponse<List<RentalCheckListResponse>> getCheckListsByRental(@PathVariable Long rentalId) {
        ApiResponse<List<RentalCheckListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListsByRental(rentalId));
        response.setMessage("Get rental checklists by rental id successfully");
        return response;
    }

    @GetMapping("/staff/{email}")
    public ApiResponse<List<RentalCheckListResponse>> getCheckListsByStaff(@PathVariable String email) {
        ApiResponse<List<RentalCheckListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListsByStaffEmail(email));
        response.setMessage("Get rental checklists by staff email successfully");
        return response;
    }

    @PostMapping
    public ApiResponse<RentalCheckListResponse> createCheckList(@RequestBody RentalCheckListCreateRequest request, @RequestParam String staffEmail ) {
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckList(request, staffEmail));
        response.setMessage("Create rental checklist successfully");
        return response;
    }

    @PutMapping
    public ApiResponse<RentalCheckListResponse> updateCheckList(@RequestBody RentalCheckListUpdateRequest request, @RequestParam String staffEmail ) {
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
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

    @PostMapping("/{rentalId}/checkin")
    public ApiResponse<RentalCheckListResponse> checkIn(
            @PathVariable Long rentalId,
            @RequestParam String staffEmail,
            @RequestBody RentalCheckListCreateRequest request) {

        request.setRentalId(rentalId);
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckIn(request, staffEmail));
        response.setMessage("Check-in successfully");
        return response;
    }

    @PostMapping("/{rentalId}/checkout")
    public ApiResponse<RentalCheckListResponse> checkOut(
            @PathVariable Long rentalId,
            @RequestParam String staffEmail,
            @RequestBody RentalCheckListCreateRequest request) {

        request.setRentalId(rentalId);
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckOut(request, staffEmail));
        response.setMessage("Check-out successfully and fee calculated");
        return response;
    }
}

