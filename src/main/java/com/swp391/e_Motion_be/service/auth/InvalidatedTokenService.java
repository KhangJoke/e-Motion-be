package com.swp391.e_Motion_be.service.auth;

import com.swp391.e_Motion_be.entity.InvalidatedToken;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.InvalidatedTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class InvalidatedTokenService {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public void addInvalidatedToken(InvalidatedToken invalidatedToken) {
        if (invalidatedTokenRepository.existsById(invalidatedToken.getId())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (invalidatedToken.getExpiryTime().before(new Date())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        invalidatedTokenRepository.save(invalidatedToken);
    }

    public boolean isTokenInvalidated(InvalidatedToken invalidatedToken) {
        return invalidatedTokenRepository.existsById(invalidatedToken.getId());
    }

    @Transactional
    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void cleanupExpiredTokens() {
        Date now = new Date();
        invalidatedTokenRepository.deleteByExpiryTimeBefore(now);
    }
}
