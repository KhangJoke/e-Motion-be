package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.user.*;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.ReservationResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.dto.responses.stats.DataAdminDashboard;
import com.swp391.e_Motion_be.dto.responses.user.FilterUserResponse;
import com.swp391.e_Motion_be.dto.responses.user.UserResponse;
import com.swp391.e_Motion_be.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> authenticateUser(){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.getCurrentUser();
        apiResponse.setMessage("Get current user successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get all users successfully");
        apiResponse.setData(userService.getAllUsers());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/search/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUserByEmailContains(@PathVariable String email){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setData(userService.getUserByEmailContains(email));
        apiResponse.setMessage("Get user by email contains successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<FilterUserResponse>> findByPageAndFilterAndSearch(@RequestBody PageAndFilterUserRequest request){
        ApiResponse<FilterUserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(userService.findByPageAndFilterAndSearch(request));
        if(apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No users found");
        }else {
            apiResponse.setMessage("Get user successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.getUserByEmail(email);
        apiResponse.setMessage("Get user by email successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/admin/delete/{email}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUserByEmail(@PathVariable String email){
        ApiResponse<String> apiResponse = new ApiResponse<>();
        userService.deleteUserByEmail(email);
        apiResponse.setStatus(204);
        apiResponse.setMessage("Delete user by email successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody @Valid ChangePasswordUserRequest request){
        userService.changePassword(request);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(204);
        apiResponse.setMessage("Change password successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/me/update-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody @Valid UpdateProfileRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.updateProfile(request);
        apiResponse.setMessage("Update profile successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DataAdminDashboard>> getDataAdminDashboard(){
        ApiResponse<DataAdminDashboard> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get data admin dashboard successfully");
        apiResponse.setData(userService.getDataAdminDashboard());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUserByAdmin(@RequestBody @Valid CreateUserRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.createUserByAdmin(request);
        apiResponse.setMessage("Create user by admin successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/admin/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserByAdmin(@RequestBody @Valid UpdateUserRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        UserResponse userResponse = userService.updateUserByAdmin(request);
        apiResponse.setMessage("update user by admin successfully");
        apiResponse.setData(userResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/admin/toggle-status/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleStatusUser(@PathVariable String email){
        userService.toggleStatusUser(email);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(204);
        apiResponse.setMessage("Block user by admin successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @GetMapping("/me/history/reservations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationHistory(){
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get user reservation history successfully");
        apiResponse.setData(userService.getReservationHistory());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/me/history/rentals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalHistory(){
        ApiResponse<List<RentalResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get user rental history successfully");
        apiResponse.setData(userService.getRentalHistory());
        return ResponseEntity.ok(apiResponse);
    }
}
