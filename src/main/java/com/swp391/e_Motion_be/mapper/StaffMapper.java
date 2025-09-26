package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.staff.StaffCreationRequest;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.entity.Staff;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    Staff staffToEntity(StaffCreationRequest request);
    StaffResponse toStaffResponse(Staff staff);
}
