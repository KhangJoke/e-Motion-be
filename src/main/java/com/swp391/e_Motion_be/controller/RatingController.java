package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.rating.RatingCreationRequest;
import com.swp391.e_Motion_be.dto.requests.rating.RatingUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.RatingResponse;
import com.swp391.e_Motion_be.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // Get all ratings
    @GetMapping
    public ApiResponse<List<RatingResponse>> getAllRatings() {
        return new ApiResponse<>(200, "success", ratingService.getAllRatings());
    }

    // Get rating by ID
    @GetMapping("/{id}")
    public ApiResponse<RatingResponse> getRatingById(@PathVariable Long id) {
        return new ApiResponse<>(200, "success", ratingService.getRatingById(id));
    }

    // Create a new rating
    @PostMapping
    public ApiResponse<RatingResponse> createRating(@RequestBody @Valid RatingCreationRequest request) {
        return new ApiResponse<>(200, "Rating created successfully", ratingService.createRating(request));
    }

    // Update rating by ID
    @PutMapping("/{id}")
    public ApiResponse<RatingResponse> updateRating(@PathVariable Long id,
                                                    @RequestBody @Valid RatingUpdateRequest request) {
        return new ApiResponse<>(200, "Rating updated successfully", ratingService.updateRating(id, request));
    }

    // Delete rating by ID
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return new ApiResponse<>(200, "Rating deleted successfully", null);
    }
}
