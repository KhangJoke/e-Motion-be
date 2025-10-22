package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.user.ChangePasswordUserRequest;
import com.swp391.e_Motion_be.dto.requests.user.UpdateProfileRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.stats.DataAdminDashboard;
import com.swp391.e_Motion_be.dto.responses.stats.StationStatsResponse;
import com.swp391.e_Motion_be.dto.responses.stats.TotalStatsResponse;
import com.swp391.e_Motion_be.dto.responses.UserResponse;
import com.swp391.e_Motion_be.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> authenticateUser(){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.getCurrentUser();
        apiResponse.setMessage("Get current user successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get all users successfully");
        apiResponse.setData(userService.getAllUsers());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.getUserByEmail(email);
        apiResponse.setMessage("Get user by email successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUserByEmail(@PathVariable String email){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        userService.deleteUserByEmail(email);
        apiResponse.setStatus(204);
        apiResponse.setMessage("Delete user by email successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody @Valid ChangePasswordUserRequest request){
        userService.changePassword(request);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(204);
        apiResponse.setMessage("Change password successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/me/update-profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody @Valid UpdateProfileRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.updateProfile(request);
        apiResponse.setMessage("Update profile successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin-dashboard")
    public ResponseEntity<ApiResponse<DataAdminDashboard>> getDataAdminDashboard(){
        ApiResponse<DataAdminDashboard> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get data admin dashboard successfully");
        apiResponse.setData(userService.getDataAdminDashboard());
        return ResponseEntity.ok(apiResponse);
    }

}
