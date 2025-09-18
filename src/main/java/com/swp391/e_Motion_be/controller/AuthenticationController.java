package com.swp391.e_Motion_be.controller;


import com.swp391.e_Motion_be.dto.requests.ForgotPasswordUserDto;
import com.swp391.e_Motion_be.dto.requests.LoginUserDto;
import com.swp391.e_Motion_be.dto.requests.RegisterUserDto;
import com.swp391.e_Motion_be.dto.requests.VerifyUserDto;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.LoginResponse;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.service.AuthenticationService;
import com.swp391.e_Motion_be.service.JwtService;
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
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(201);
        apiResponse.setMessage("User registered successfully. Please check your email for verification code.");
        apiResponse.setData(registeredUser.getEmail());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginUserDto loginUserDto) {
        User loginUser = authenticationService.authenticate(loginUserDto);
        String token = jwtService.generateToken(loginUser);
        LoginResponse loginResponse = new LoginResponse(token, jwtService.extractExpiration(token).getTime());
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(200);
        apiResponse.setMessage("User logged in successfully");
        apiResponse.setData(loginResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyUser(@RequestBody VerifyUserDto verifyUserDto ) {
        try{
            authenticationService.verifyUser(verifyUserDto);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setMessage("User verified successfully");
            apiResponse.setData(verifyUserDto.getEmail());
            return ResponseEntity.ok(apiResponse);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(new ApiResponse(401, e.getMessage(), null));
        }
    }

    @PostMapping("/forgotPassword/verify")
    public ResponseEntity<ApiResponse> verifyForgotPasswordUser(@RequestBody VerifyUserDto verifyUserDto ) {
        try{
            authenticationService.verifyForgotPasswordUser(verifyUserDto);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setMessage("User verified successfully");
            apiResponse.setData(verifyUserDto.getEmail());
            return ResponseEntity.ok(apiResponse);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(new ApiResponse(401, e.getMessage(), null));
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse> resendVerificationCode(@RequestParam String email ) {
        try{
            authenticationService.resendVerificationCode(email);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setMessage("Verification code resent successfully");
            apiResponse.setData(email);
            return ResponseEntity.ok(apiResponse);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(new ApiResponse(401, e.getMessage(), null));
        }
    }

    @PostMapping("/forgotPassword/sendVerify/{email}")
    public ResponseEntity<ApiResponse> verifyForgotPasswordUser(@PathVariable String email) {
        try{
            authenticationService.sendVerificationEmailToUpdatePassword(email);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setMessage("Verification code sent successfully");
            apiResponse.setData(email);
            return ResponseEntity.ok(apiResponse);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ApiResponse(401, e.getMessage(), null));
        }
    }

    @PostMapping("/forgotPassword/update")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody ForgotPasswordUserDto input) {
        authenticationService.updatePassword(input);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Password updated successfully");
        apiResponse.setData(input.getEmail());
        return ResponseEntity.ok(apiResponse);
    }
}
