package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.UserResponse;
import com.swp391.e_Motion_be.service.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> authenticateUser(){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.getCurrentUser();
        apiResponse.setMessage("Get current user successfully");
        apiResponse.setData(userResponse);
        return apiResponse;
    }

    @GetMapping()
    public ApiResponse<List<UserResponse>> getAllUsers(){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get all users successfully");
        apiResponse.setData(userService.getAllUsers());
        return apiResponse;
    }
}
