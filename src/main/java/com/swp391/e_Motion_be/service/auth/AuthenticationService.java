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
import com.swp391.e_Motion_be.repository.UserRepository;
import com.swp391.e_Motion_be.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    @Value("${google.client-id:}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RedisTokenService redisTokenService;

    public User signup(RegisterUserDto input) {
        User oldUser = userRepository.findByEmail(input.getEmail()).orElse(null);

        if (oldUser != null && oldUser.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        } else if (oldUser != null && !oldUser.isEnabled()) {
            oldUser.setFullName(input.getFullName());
            oldUser.setPhone(input.getPhone());
            oldUser.setVerificationCode(generateVerificationCode());
            oldUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            oldUser.setPassword(passwordEncoder.encode(input.getUserPassword()));
            emailService.sendVerificationEmail(oldUser);
            return userRepository.save(oldUser);
        }
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        } else if (userRepository.findByPhoneAndEnabledTrue(input.getPhone()) != null) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        User user = userMapper.toUser(input);
        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(input.getUserPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        emailService.sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public void logout(String refreshToken, String accessToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.NOT_LOGIN_YET);
        }

        RefreshToken token = refreshTokenService.findByToken(refreshToken);
        if (token != null) {
            refreshTokenService.revokeToken(token);
        }

        if (jwtService.extractExpiration(accessToken).before(new Date())) {
            return;
        }

        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtService.extractJwtId(accessToken))
                .expiredTime(jwtService.extractExpiration(accessToken).getTime() - new Date().getTime())
                .build();

        redisTokenService.save(redisToken);
        log.info("Logout success");
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        } else if (user.isBlocked()) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()));
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFY_EXPIRED);
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
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

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getForgotPasswordCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFY_EXPIRED);
            }
            if (user.getForgotPasswordCode().equals(input.getVerificationCode())) {
                user.setForgotPasswordCode(null);
                userRepository.save(user);
            } else {
                throw new AppException(ErrorCode.VERIFY_CODE_NOT_MATCH);
            }
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setRole(Role.ROLE_USER);
            newUser.setEnabled(false);
            return userRepository.save(newUser);
        });

        if (user.isBlocked()) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendVerificationEmail(user);
    }

    public User verifyOtp(VerifyUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        if (user.getVerificationCodeExpiresAt() == null
                || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.VERIFY_EXPIRED);
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(input.getVerificationCode())) {
            throw new AppException(ErrorCode.VERIFY_CODE_NOT_MATCH);
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        return userRepository.save(user);
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new AppException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendVerificationEmail(user);
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void sendVerificationEmailToUpdatePassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!user.isEnabled()) {
                throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
            }
            user.setForgotPasswordCode(generateVerificationCode());
            user.setForgotPasswordCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendVerificationEmail(user);
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public void updatePassword(ForgotPasswordUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getForgotPasswordCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFY_EXPIRED);
            }
            user.setPassword(passwordEncoder.encode(input.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    public User authenticateGoogle(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new AppException(ErrorCode.GOOGLE_TOKEN_EMPTY);
        }

        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken.trim();
        Map<String, Object> payload;
        try {
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            payload = response;
        } catch (HttpClientErrorException e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        } catch (Exception e) {
            log.error("Error communicating with Google OAuth service: {}", e.getMessage());
            throw new AppException(ErrorCode.GOOGLE_AUTH_FAILED);
        }

        if (payload == null || payload.containsKey("error")) {
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        if (googleClientId != null && !googleClientId.trim().isEmpty()) {
            String aud = (String) payload.get("aud");
            if (aud == null || !googleClientId.contains(aud)) {
                log.warn("Google token audience mismatch. Configured: {}, Token aud: {}", googleClientId, aud);
                throw new AppException(ErrorCode.GOOGLE_AUDIENCE_MISMATCH);
            }
        }

        String email = (String) payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            log.error("Google token does not contain an email");
            throw new AppException(ErrorCode.GOOGLE_AUTH_FAILED);
        }

        Object emailVerifiedObj = payload.get("email_verified");
        boolean emailVerified = emailVerifiedObj instanceof Boolean
                ? (Boolean) emailVerifiedObj
                : Boolean.parseBoolean(String.valueOf(emailVerifiedObj));
        if (!emailVerified) {
            throw new AppException(ErrorCode.GOOGLE_EMAIL_NOT_VERIFIED);
        }

        String name = (String) payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            name = (String) payload.get("given_name");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (user.isBlocked()) {
                throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
            }

            boolean needsUpdate = false;
            if (!user.isEnabled()) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                needsUpdate = true;
            }
            if ((user.getFullName() == null || user.getFullName().trim().isEmpty())
                    && name != null && !name.trim().isEmpty()) {
                user.setFullName(name);
                needsUpdate = true;
            }
            if (needsUpdate) {
                user = userRepository.save(user);
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setFullName(name != null && !name.trim().isEmpty() ? name : "Google User");
            user.setRole(Role.ROLE_USER);
            user.setEnabled(true);
            user.setBlocked(false);
            user.setPoint(0);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user = userRepository.save(user);
        }

        return user;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
