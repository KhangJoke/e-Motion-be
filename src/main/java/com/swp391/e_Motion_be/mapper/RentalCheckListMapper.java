package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.rentalCheckList.RentalCheckInResponse;
import com.swp391.e_Motion_be.dto.responses.rentalCheckList.RentalCheckOutResponse;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalCheckListMapper {
    RentalCheckList toCheckListEntity(RentalCheckListCreateRequest request);
    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(source = "staff.user.email", target = "staffEmail")
    RentalCheckOutResponse toRentalCheckOutResponse(RentalCheckList rentalCheckOut);
    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(source = "staff.user.email", target = "staffEmail")
    RentalCheckInResponse toRentalCheckInResponse(RentalCheckList rentalCheckIn);
}
