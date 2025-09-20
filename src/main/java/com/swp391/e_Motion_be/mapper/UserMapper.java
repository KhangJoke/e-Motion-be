package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.user.RegisterUserDto;
import com.swp391.e_Motion_be.dto.responses.UserResponse;
import com.swp391.e_Motion_be.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    User toUser(RegisterUserDto registerUserDto);
}
