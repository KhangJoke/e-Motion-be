package com.swp391.e_Motion_be.service.auth;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    // key: jti, value: expiry
    private final Map<String, Date> blacklist;

    private final JwtService jwtService;

    public TokenBlacklistService(JwtService jwtService) {
        this.blacklist = new ConcurrentHashMap<>();
        this.jwtService = jwtService;
    }

    public void addToken(String token) {
        blacklist.put(jwtService.extractId(token), jwtService.extractExpiration(token));
    }

    public boolean isBlacklisted(String token) {
        String jti = jwtService.extractId(token);
        Date exp = blacklist.get(jti);
        if (exp == null) return false;

        if (exp.before(new Date())) {
            blacklist.remove(jti); // dọn token hết hạn
            return false;
        }
        return true;
    }
}

