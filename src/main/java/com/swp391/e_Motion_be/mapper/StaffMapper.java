package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "station.name", target = "stationName")
    @Mapping(source = "user.fullName", target = "fullName")
    StaffResponse toStaffResponse(Staff staff);
}
