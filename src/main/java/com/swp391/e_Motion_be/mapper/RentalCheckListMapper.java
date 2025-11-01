package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListListResponse;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalCheckListMapper {
    RentalCheckList toCheckListEntity(RentalCheckListCreateRequest request);
    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(source = "staff.user.email", target = "staffEmail")
    RentalCheckListResponse toRentalCheckListResponse(RentalCheckList rentalCheckOut);
    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(source = "staff.user.email", target = "staffEmail")
    RentalCheckListListResponse toRentalCheckListListResponse(RentalCheckList rentalCheckList);
}
