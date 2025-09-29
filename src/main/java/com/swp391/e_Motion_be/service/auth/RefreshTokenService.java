package com.swp391.e_Motion_be.service.auth;

import com.swp391.e_Motion_be.entity.RefreshToken;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public String CreateAndStore (User user) {
        String token = generateRawRefreshToken();
        String hashedToken = sha256Hash(token);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(hashedToken);
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public RefreshToken findByToken(String token) {
        String hashedToken = sha256Hash(token);
        return refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeAllTokensForUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(tokens);
    }

    public RefreshToken rotateRefreshToken(RefreshToken old) {
        String newToken = generateRawRefreshToken();
        String hashedNewToken = sha256Hash(newToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(hashedNewToken);
        newRefreshToken.setUser(old.getUser());
        newRefreshToken.setCreatedAt(LocalDateTime.now());
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        old.setRevoked(true);
        old.setReplacedBy(newRefreshToken.getId());

        refreshTokenRepository.save(old);
        refreshTokenRepository.save(newRefreshToken);

        return newRefreshToken;
    }

    public String generateRawRefreshToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String sha256Hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing token", e);
        }
    }
}
