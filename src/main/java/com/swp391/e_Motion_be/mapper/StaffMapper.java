package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.staff.StaffCreationRequest;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(source = "request.userId", target = "user.id")
    @Mapping(source = "request.stationId", target = "station.id")
    Staff staffCreationRequestToEntity(StaffCreationRequest request);

    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "user.id", target = "userId")
    StaffResponse toStaffResponse(Staff staff);
}
