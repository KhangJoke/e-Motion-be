package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.service.DocuSealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final DocuSealService docuSealService;
    private final RentalRepository rentalRepository;

    @PostMapping("/create/{rentalId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> createContract(@PathVariable long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        String contractUrl = docuSealService.createContract(rental);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Contract created successfully");
        response.setData(contractUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody Map<String, Object> payload) {
        docuSealService.handleWebhook(payload);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Webhook processed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view/{rentalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> viewContract(@PathVariable long rentalId) {
        String contractUrl = docuSealService.getContractUrl(rentalId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Contract URL retrieved successfully");
        response.setData(contractUrl);
        return ResponseEntity.ok(response);
    }
}
