package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RentalCheckListMapper {
    RentalCheckList toCheckListEntity(RentalCheckListCreateRequest request);
    RentalCheckListResponse toRentalCheckListResponse(RentalCheckList rentalCheckList);
}
