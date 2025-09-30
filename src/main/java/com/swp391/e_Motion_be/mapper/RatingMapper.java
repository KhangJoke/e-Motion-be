package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.rating.RatingCreationRequest;
import com.swp391.e_Motion_be.dto.requests.rating.RatingUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.RatingResponse;
import com.swp391.e_Motion_be.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "rentalId", target = "rental.id")
    Rating toEntity(RatingCreationRequest request);

    // Convert entity -> response
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "rental.id", target = "rentalId")
    RatingResponse toResponse(Rating entity);

    // Update entity from update request
    void updateEntityFromRequest(@MappingTarget Rating entity, RatingUpdateRequest request);
}
