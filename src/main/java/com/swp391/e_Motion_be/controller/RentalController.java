package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.rental.*;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.rental.PageAndFilterRentalResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalListResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalOverviewResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.service.RentalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<RentalListResponse>> getAllRentals() {
        ApiResponse<List<RentalListResponse>> response = new ApiResponse<>();
        response.setData(rentalService.getAllRentals());
        return response;
    }

    @PostMapping("/reservation")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> createRentalFromReservation(@RequestBody @Valid RentalCreateFromReservationRequest request){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.createRentalFromReservation(request));
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> createRental(@RequestBody @Valid RentalCreateRequest request){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.createRental(request));
        return response;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<RentalResponse>> getRentalsByStatus(@PathVariable String status){
        ApiResponse<List<RentalResponse>> response = new ApiResponse<>();
        response.setData(rentalService.getRentalsByStatus(status));
        return response;
    }

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> updateRentalStatus(@RequestBody @Valid RentalUpdateStatusRequest request){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.updateRentalStatus(request));
        return response;
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> getRentalDetails(@PathVariable Long id){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.getRentalById(id));
        return response;
    }

    @GetMapping("/{id}/overview")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalOverviewResponse> getRentalOverview(@PathVariable Long id){
        ApiResponse<RentalOverviewResponse> response = new ApiResponse<>();
        response.setData(rentalService.getRentalOverviewById(id));
        return response;
    }

    @PostMapping("/{id}/check-inpayment")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<String> processCheckInPayment(@PathVariable Long id, HttpServletRequest request) throws Exception {
        ApiResponse<String> response = new ApiResponse<>();
        response.setData(rentalService.processCheckInPayment(id , request.getRemoteAddr()));
        return response;
    }

    @PostMapping("/{id}/check-outpayment")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<CheckOutProcessResponse> processCheckOutPayment(@PathVariable Long id, HttpServletRequest request){
        ApiResponse<CheckOutProcessResponse> response = new ApiResponse<>();
        response.setData(rentalService.processCheckOutPayment(id , request.getRemoteAddr()));
        return response;
    }

    //Chưa làm phân quyền
    @PostMapping("/{id}/extend")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> extendRentalReturnTime(@PathVariable Long id, @RequestParam LocalDateTime newReturnTime, HttpServletRequest request) throws Exception {
        ApiResponse<String> response = new ApiResponse<>();
        response.setData(rentalService.extendRentalReturnTime(id, newReturnTime, request.getRemoteAddr()));
        return response;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<RentalResponse>> searchRentals (@RequestParam String email,
                                                            @RequestParam (required = false) List<RentalStatus> status) {
        List<RentalResponse> rentalResponses = null;
        if(status != null ) {
            rentalResponses = rentalService.getRentalByEmailUserContainAndStatusIn(email, status);
        }else{
            rentalResponses =  rentalService.getRentalByEmailUserContain(email);
        }
        ApiResponse<List<RentalResponse>> response = new ApiResponse<>();
        response.setData(rentalResponses);
        response.setMessage("Fetched reservation successfully");
        response.setStatus(200);

        return response;
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageAndFilterRentalResponse>> findByPageAndFilterAndSearch(@RequestBody PageAndFilterRentalRequest request){
        ApiResponse<PageAndFilterRentalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(rentalService.findByPageAndFilterAndSearch(request));
        if(apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No rentals found");
        }else {
            apiResponse.setMessage("Get rentals successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }
}
