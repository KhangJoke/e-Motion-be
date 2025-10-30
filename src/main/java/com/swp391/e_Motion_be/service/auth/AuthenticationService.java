package com.swp391.e_Motion_be.service.auth;

import com.swp391.e_Motion_be.dto.requests.auth.LoginUserDto;
import com.swp391.e_Motion_be.dto.requests.auth.RegisterUserDto;
import com.swp391.e_Motion_be.dto.requests.auth.VerifyUserDto;
import com.swp391.e_Motion_be.dto.requests.user.ForgotPasswordUserDto;
import com.swp391.e_Motion_be.entity.RedisToken;
import com.swp391.e_Motion_be.entity.RefreshToken;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.UserMapper;
import com.swp391.e_Motion_be.repository.RedisTokenRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import com.swp391.e_Motion_be.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RedisTokenRepository redisTokenRepository;

    public User signup(RegisterUserDto input) {
        User oldUser = userRepository.findByEmail(input.getEmail()).orElse(null);

        if(oldUser != null && oldUser.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        }else if(oldUser != null && !oldUser.isEnabled()){
            oldUser.setFullName(input.getFullName());
            oldUser.setPhone(input.getPhone());
            oldUser.setVerificationCode(generateVerificationCode());
            oldUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            oldUser.setPassword(passwordEncoder.encode(input.getUserPassword()));
            sendVerificationEmail(oldUser);
            return userRepository.save(oldUser);
        }
        if(userRepository.existsByEmail(input.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        } else if(userRepository.existsByPhone(input.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        User user = userMapper.toUser(input);
        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(input.getUserPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public void logout(String refreshToken, String accessToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.NOT_LOGIN_YET);
        }

        RefreshToken token = refreshTokenService.findByToken(refreshToken);
        if (token != null) {
            refreshTokenService.revokeToken(token);
        }

        if(jwtService.extractExpiration(accessToken).before(new Date())) {
            return;
        }

        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtService.extractJwtId(accessToken))
                .expiredTime(jwtService.extractExpiration(accessToken).getTime() - new Date().getTime())
                .build();

        redisTokenRepository.save(redisToken);
        log.info("Logout success");
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        if(!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }else if(user.isBlocked()) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());

        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFY_EXPIRED);
            }
            if(user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new AppException(ErrorCode.VERIFY_CODE_NOT_MATCH);
            }
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void verifyForgotPasswordUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());

        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFY_EXPIRED);
            }
            if(user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setForgotPasswordCode(null);
                user.setForgotPasswordCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new AppException(ErrorCode.VERIFY_CODE_NOT_MATCH);
            }
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user.isEnabled()) {
                throw new AppException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            sendVerificationEmail(user);
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void sendVerificationEmailToUpdatePassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(!user.isEnabled()) {
                throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
            }
            user.setForgotPasswordCode(generateVerificationCode());
            user.setForgotPasswordCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            sendVerificationEmail(user);
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void updatePassword(ForgotPasswordUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user.getForgotPasswordCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFY_EXPIRED);
            }
            if(!user.getForgotPasswordCode().equals(input.getForgotPasswordCode())) {
                throw new AppException(ErrorCode.VERIFY_CODE_NOT_MATCH);
            }
            user.setPassword(passwordEncoder.encode(input.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; "
                + "box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">"
                + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</html>";
        try{
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        }
        catch (MessagingException e){
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
