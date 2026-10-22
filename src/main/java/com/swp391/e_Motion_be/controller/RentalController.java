package com.swp391.e_Motion_be.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swp391.e_Motion_be.dto.requests.rental.CheckOutProcessResponse;
import com.swp391.e_Motion_be.dto.requests.rental.PageAndFilterRentalHistoryRequest;
import com.swp391.e_Motion_be.dto.requests.rental.PageAndFilterRentalRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalUpdateStatusRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.VnpayResponse;
import com.swp391.e_Motion_be.dto.responses.rental.ContractConfirmResponse;
import com.swp391.e_Motion_be.dto.responses.rental.PageAndFilterRentalHistoryResponse;
import com.swp391.e_Motion_be.dto.responses.rental.PageAndFilterRentalResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalListResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalOverviewResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.service.DocuSealService;
import com.swp391.e_Motion_be.service.RentalService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final DocuSealService docuSealService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<RentalListResponse>> getAllRentals() {
        ApiResponse<List<RentalListResponse>> response = new ApiResponse<>();
        response.setData(rentalService.getAllRentals());
        return response;
    }

    @PostMapping("/reservation")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> createRentalFromReservation(
            @RequestBody @Valid RentalCreateFromReservationRequest request) {
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.createRentalFromReservation(request));
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> createRental(@RequestBody @Valid RentalCreateRequest request) {
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.createRental(request));
        return response;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<RentalResponse>> getRentalsByStatus(@PathVariable String status) {
        ApiResponse<List<RentalResponse>> response = new ApiResponse<>();
        response.setData(rentalService.getRentalsByStatus(status));
        return response;
    }

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> updateRentalStatus(@RequestBody @Valid RentalUpdateStatusRequest request) {
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.updateRentalStatus(request));
        return response;
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<RentalResponse> getRentalDetails(@PathVariable Long id) {
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.getRentalById(id));
        return response;
    }

    @GetMapping("/me/details/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RentalResponse> getOwnRentalDetails(@PathVariable Long id) {
        ApiResponse<RentalResponse> response = new ApiResponse<>();
        response.setData(rentalService.getOwnRentalDetails(id));
        return response;
    }

    @GetMapping("/{id}/overview")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') or isAuthenticated()")
    public ApiResponse<RentalOverviewResponse> getRentalOverview(@PathVariable Long id) {
        ApiResponse<RentalOverviewResponse> response = new ApiResponse<>();
        response.setData(rentalService.getRentalOverviewById(id));
        return response;
    }

    @PostMapping("/{id}/contract")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> createContract(@PathVariable Long id) {
        String contractUrl = docuSealService.createContract(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Contract created successfully");
        response.setData(contractUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping({ "/{id}/contract/confirm-signed", "/{id}/confirm-signed" })
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') or isAuthenticated()")
    public ResponseEntity<ApiResponse<ContractConfirmResponse>> confirmContractSigned(@PathVariable Long id) {
        ContractConfirmResponse confirmResponse = docuSealService.confirmContractSigned(id);
        ApiResponse<ContractConfirmResponse> response = new ApiResponse<>();
        response.setMessage("Contract signed confirmed successfully");
        response.setData(confirmResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-inpayment")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<VnpayResponse> processCheckInPayment(@RequestParam("rentalId") Long id,
            @RequestParam("point") int point, HttpServletRequest request) throws Exception {
        ApiResponse<VnpayResponse> response = new ApiResponse<>();
        response.setData(rentalService.processCheckInPayment(id, point, request.getRemoteAddr()));
        return response;
    }

    @PostMapping("/{id}/check-outpayment")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') or isAuthenticated()")
    public ApiResponse<CheckOutProcessResponse> processCheckOutPayment(@PathVariable Long id,
            HttpServletRequest request) {
        ApiResponse<CheckOutProcessResponse> response = new ApiResponse<>();
        response.setData(rentalService.processCheckOutPayment(id, request.getRemoteAddr()));
        return response;
    }

    // Chưa làm phân quyền
    @PostMapping("/{id}/extend")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<VnpayResponse> extendRentalReturnTime(@PathVariable Long id,
            @RequestBody LocalDateTime newReturnTime, HttpServletRequest request) throws Exception {
        ApiResponse<VnpayResponse> response = new ApiResponse<>();
        response.setData(rentalService.extendRentalReturnTime(id, newReturnTime, request.getRemoteAddr()));
        return response;
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalOfStation(@PathVariable Long stationId) {
        ApiResponse<List<RentalResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setData(rentalService.getRentalOfStation(stationId));
        apiResponse.setMessage("Get station rentals successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageAndFilterRentalHistoryResponse>> getRentalsByUserEmail(
            @RequestBody PageAndFilterRentalHistoryRequest request) {
        ApiResponse<PageAndFilterRentalHistoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get user rental history successfully");
        apiResponse.setData(rentalService.getRentalsByUserEmail(request));
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageAndFilterRentalResponse>> findByPageAndFilterAndSearch(
            @RequestBody PageAndFilterRentalRequest request) {
        ApiResponse<PageAndFilterRentalResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(rentalService.findByPageAndFilterAndSearch(request));
        if (apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No rentals found");
        } else {
            apiResponse.setMessage("Get rentals successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/manage/{rentalId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Boolean> cancelRentalByManager(@PathVariable long rentalId) {
        ApiResponse<Boolean> response = new ApiResponse<>();
        response.setData(rentalService.cancelRental(rentalId));
        response.setMessage("Cancelled rental successfully");
        response.setStatus(200);
        return response;
    }
}
