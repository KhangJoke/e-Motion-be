package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.brand.BrandCreationRequest;
import com.swp391.e_Motion_be.dto.requests.brand.BrandUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.brand.BrandResponse;
import com.swp391.e_Motion_be.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    Brand toBrandEntity(BrandCreationRequest request);
    BrandResponse toBrandResponse(Brand brand);
    void updateBrandFromRequest(BrandUpdateRequest request, @MappingTarget Brand brand);
}
