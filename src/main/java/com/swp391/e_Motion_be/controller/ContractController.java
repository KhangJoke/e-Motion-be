package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.responses.ApiResponse;
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

    @PostMapping("/webhook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody Map<String, Object> payload) {
        docuSealService.handleWebhook(payload);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Webhook processed successfully");
        return ResponseEntity.ok(response);
    }
}
