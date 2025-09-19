package com.swp391.e_Motion_be.service.user;

import com.swp391.e_Motion_be.dto.responses.UserResponse;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.mapper.UserMapper;
import com.swp391.e_Motion_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> userMapper.toUserResponse(user))
                .toList();
    }

}
