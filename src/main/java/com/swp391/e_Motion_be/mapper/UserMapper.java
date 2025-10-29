package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.auth.RegisterUserDto;
import com.swp391.e_Motion_be.dto.responses.user.UserResponse;
import com.swp391.e_Motion_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {DocumentMapper.class})
public interface UserMapper {
    UserResponse toUserResponse(User user);

    @Mapping(target = "documents", ignore = true)
    UserResponse toUserResponseWithoutDocument(User user);

    @Mapping(source = "staff.station.name", target = "stationName")
    @Mapping(source = "staff.id", target = "staffId")
    UserResponse toStaffResponse(User user);

    User toUser(RegisterUserDto registerUserDto);
}
