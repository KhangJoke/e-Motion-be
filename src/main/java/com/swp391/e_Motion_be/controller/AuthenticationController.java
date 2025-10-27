package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.auth.LoginUserDto;
import com.swp391.e_Motion_be.dto.requests.auth.RegisterUserDto;
import com.swp391.e_Motion_be.dto.requests.auth.VerifyUserDto;
import com.swp391.e_Motion_be.dto.requests.user.ForgotPasswordUserDto;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.LoginResponse;
import com.swp391.e_Motion_be.entity.RefreshToken;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.service.auth.AuthenticationService;
import com.swp391.e_Motion_be.service.auth.JwtService;
import com.swp391.e_Motion_be.service.auth.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(201);
        apiResponse.setMessage("User registered successfully. Please check your email for verification code.");
        apiResponse.setData(registeredUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        User loginUser = authenticationService.authenticate(loginUserDto);
        String accessToken = jwtService.generateToken(loginUser);
        LoginResponse loginResponse = new LoginResponse(accessToken, jwtService.extractExpiration(accessToken).getTime());
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(200, "User logged in successfully", loginResponse);

        // Create and store refresh token in HttpOnly cookie
        String refreshToken = refreshTokenService.CreateAndStore(loginUser);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .domain("localhost")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@CookieValue(name="refresh_token", required = false) String refreshToken,
                                                            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.SENDED_TOKEN_NOT_FOUND);
        }
        // Rotate refresh token
        RefreshToken oldRefreshToken = refreshTokenService.findByToken(refreshToken);
        if(oldRefreshToken.isRevoked() || oldRefreshToken.getReplacedBy() != null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_IS_REUSED);
        }
        String newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .domain("localhost")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String newAccessToken = jwtService.generateToken(oldRefreshToken.getUser());
        LoginResponse loginResponse = new LoginResponse(newAccessToken, jwtService.extractExpiration(newAccessToken).getTime());
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(200, "Token refreshed successfully", loginResponse);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name="refresh_token", required=false) String refreshToken,
                                                    @RequestHeader("Authorization") String authHeader,
                                                    HttpServletResponse response)
    {
        String accessToken = authHeader.replace("Bearer ", "");
        if (refreshToken == null || refreshToken.isEmpty() || accessToken.isEmpty()) {
            throw new AppException(ErrorCode.SENDED_TOKEN_NOT_FOUND);
        }
        authenticationService.logout(refreshToken, accessToken);
        ApiResponse<Void> apiResponse = new ApiResponse<>( 204, "User logged out successfully", null);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/verify")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> verifyUser(@RequestBody VerifyUserDto verifyUserDto ) {
        authenticationService.verifyUser(verifyUserDto);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("User verified successfully");
        apiResponse.setData(verifyUserDto.getEmail());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgotPassword/verify")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> verifyForgotPasswordUser(@RequestBody VerifyUserDto verifyUserDto ) {
        authenticationService.verifyForgotPasswordUser(verifyUserDto);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("User verified successfully");
        apiResponse.setData(verifyUserDto.getEmail());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/resend")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@RequestBody String email ) {
        authenticationService.resendVerificationCode(email);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Verification code resent successfully");
        apiResponse.setData(email);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgotPassword/sendVerify/{email}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> verifyForgotPasswordUser(@PathVariable String email) {
        authenticationService.sendVerificationEmailToUpdatePassword(email);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Verification code sent successfully");
        apiResponse.setData(email);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgotPassword/update")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> updatePassword(@RequestBody ForgotPasswordUserDto input) {
        authenticationService.updatePassword(input);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(200);
        apiResponse.setMessage("Password updated successfully");
        apiResponse.setData(input.getEmail());
        return ResponseEntity.ok(apiResponse);
    }
}
