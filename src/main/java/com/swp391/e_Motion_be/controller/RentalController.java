package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.RentalResponse;
import com.swp391.e_Motion_be.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
//    Post http://localhost:8080/api/rentals/notify-expiring
//    @PostMapping("/notify-expiring")
//    public List<RentalResponse> notifyExpiring() {
//        return rentalService.notifyExpiringRentals();
//    }

}
