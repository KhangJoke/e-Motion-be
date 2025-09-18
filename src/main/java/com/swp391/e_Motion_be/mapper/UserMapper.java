package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.user.RegisterUserDto;
import com.swp391.e_Motion_be.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toRegisterUserDto(RegisterUserDto request);
}
