package com.swp391.e_Motion_be.service.user;

import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
