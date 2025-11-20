package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.rating.RatingCreationRequest;
import com.swp391.e_Motion_be.dto.requests.rating.RatingUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.RatingResponse;
import com.swp391.e_Motion_be.entity.Rating;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RatingMapper;
import com.swp391.e_Motion_be.repository.RatingRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    // CREATE
    public RatingResponse createRating(RatingCreationRequest request) {
        Rating rating = ratingMapper.toEntity(request);

        // fetch entities
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        // set relations
        rating.setUser(user);
        rating.setRental(rental);
        Rating saved = ratingRepository.save(rating);
        return ratingMapper.toResponse(saved);
    }

    // GET (get by id)
    public RatingResponse getRatingById(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_ID_NOT_FOUND));
        return ratingMapper.toResponse(rating);
    }

    // GET ALL
    public List<RatingResponse> getAllRatings() {
        return ratingRepository.findAll()
                .stream()
                .map(ratingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // UPDATE
    public RatingResponse updateRating(Long id, RatingUpdateRequest request) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_ID_NOT_FOUND));

        ratingMapper.updateEntityFromRequest(rating, request);
        Rating updated = ratingRepository.save(rating);
        return ratingMapper.toResponse(updated);
    }

    // DELETE
    public void deleteRating(Long id) {
        if (!ratingRepository.existsById(id)) {
            throw new AppException(ErrorCode.RATING_ID_NOT_FOUND);
        }
        ratingRepository.deleteById(id);
    }
}
