package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.rental.CheckOutProcessResponse;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalUpdateStatusRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalOverviewResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.service.RentalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    public ApiResponse<List<RentalResponse>> getAllRentals() {
        ApiResponse<List<RentalResponse>> response = new ApiResponse<>();
        response.setData(rentalService.getAllRentals());
        return response;
    }

    @PostMapping("/reservation")
    public ApiResponse<RentalResponse> createRentalFromReservation(@RequestBody @Valid RentalCreateFromReservationRequest request){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.createRentalFromReservation(request));
        return response;
    }

    @PostMapping
    public ApiResponse<RentalResponse> createRental(@RequestBody @Valid RentalCreateRequest request){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.createRental(request));
        return response;
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<RentalResponse>> getRentalsByStatus(@PathVariable String status){
        ApiResponse<List<RentalResponse>> response = new ApiResponse<>();
        response.setData(rentalService.getRentalsByStatus(status));
        return response;
    }

    @PatchMapping("/status")
    public ApiResponse<RentalResponse> updateRentalStatus(@RequestBody @Valid RentalUpdateStatusRequest request){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.updateRentalStatus(request));
        return response;
    }

    @GetMapping("/{id}/details")
    public ApiResponse<RentalResponse> getRentalDetails(@PathVariable Long id){
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.getRentalById(id));
        return response;
    }

    @GetMapping("/{id}/overview")
    public ApiResponse<RentalOverviewResponse> getRentalOverview(@PathVariable Long id){
        ApiResponse<RentalOverviewResponse> response = new ApiResponse<>();
        response.setData(rentalService.getRentalOverviewById(id));
        return response;
    }

    @PostMapping("/{id}/check-inpayment")
    public ApiResponse<String> processCheckInPayment(@PathVariable Long id, HttpServletRequest request) throws Exception {
        ApiResponse<String> response = new ApiResponse<>();
        response.setData(rentalService.processCheckInPayment(id , request.getRemoteAddr()));
        return response;
    }

    @PostMapping("/{id}/check-outpayment")
    public ApiResponse<CheckOutProcessResponse> processCheckOutPayment(@PathVariable Long id, HttpServletRequest request){
        ApiResponse<CheckOutProcessResponse> response = new ApiResponse<>();
        response.setData(rentalService.processCheckOutPayment(id , request.getRemoteAddr()));
        return response;
    }

    @PostMapping("/{id}/extend")
    public ApiResponse<String> extendRentalReturnTime(@PathVariable Long id, @RequestParam LocalDateTime newReturnTime, HttpServletRequest request) throws Exception {
        ApiResponse<String> response = new ApiResponse<>();
        response.setData(rentalService.extendRentalReturnTime(id, newReturnTime, request.getRemoteAddr()));
        return response;
    }

    @GetMapping("/search")
    public ApiResponse<List<RentalResponse>> searchRentals (@RequestParam String email,
                                                            @RequestParam (required = false) RentalStatus status) {
        List<RentalResponse> rentalResponses = null;
        if(status != null ) {
            rentalResponses = rentalService.getRentalByEmailUserContainAndStatus(email, status);
        }else{
            rentalResponses =  rentalService.getRentalByEmailUserContain(email);
        }
        ApiResponse<List<RentalResponse>> response = new ApiResponse<>();
        response.setData(rentalResponses);
        response.setMessage("Fetched reservation successfully");
        response.setStatus(200);

        return response;
    }
}
