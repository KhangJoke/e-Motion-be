package com.swp391.e_Motion_be.controller;


import com.swp391.e_Motion_be.dto.requests.user.ForgotPasswordUserDto;
import com.swp391.e_Motion_be.dto.requests.user.LoginUserDto;
import com.swp391.e_Motion_be.dto.requests.user.RegisterUserDto;
import com.swp391.e_Motion_be.dto.requests.user.VerifyUserDto;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.LoginResponse;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.service.auth.AuthenticationService;
import com.swp391.e_Motion_be.service.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(201);
        apiResponse.setMessage("User registered successfully. Please check your email for verification code.");
        apiResponse.setData(registeredUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginUserDto loginUserDto) {
        User loginUser = authenticationService.authenticate(loginUserDto);
        String token = jwtService.generateToken(loginUser);
        LoginResponse loginResponse = new LoginResponse(token, jwtService.extractExpiration(token).getTime());
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("User logged in successfully");
        apiResponse.setData(loginResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyUser(@RequestBody VerifyUserDto verifyUserDto ) {
        authenticationService.verifyUser(verifyUserDto);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("User verified successfully");
        apiResponse.setData(verifyUserDto.getEmail());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgotPassword/verify")
    public ResponseEntity<ApiResponse<String>> verifyForgotPasswordUser(@RequestBody VerifyUserDto verifyUserDto ) {
        authenticationService.verifyForgotPasswordUser(verifyUserDto);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("User verified successfully");
        apiResponse.setData(verifyUserDto.getEmail());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@RequestParam String email ) {
        authenticationService.resendVerificationCode(email);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Verification code resent successfully");
        apiResponse.setData(email);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgotPassword/sendVerify/{email}")
    public ResponseEntity<ApiResponse<String>> verifyForgotPasswordUser(@PathVariable String email) {
        authenticationService.sendVerificationEmailToUpdatePassword(email);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Verification code sent successfully");
        apiResponse.setData(email);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgotPassword/update")
    public ResponseEntity<ApiResponse<String>> updatePassword(@RequestBody ForgotPasswordUserDto input) {
        authenticationService.updatePassword(input);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Password updated successfully");
        apiResponse.setData(input.getEmail());
        return ResponseEntity.ok(apiResponse);
    }
}
