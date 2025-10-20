package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.auth.RegisterUserDto;
import com.swp391.e_Motion_be.dto.responses.UserResponse;
import com.swp391.e_Motion_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {DocumentMapper.class})
public interface UserMapper {
    @Mapping(source = "staff.id", target = "staffId")
    UserResponse toUserResponse(User user);
    User toUser(RegisterUserDto registerUserDto);
}
