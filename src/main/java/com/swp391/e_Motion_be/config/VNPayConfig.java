package com.swp391.e_Motion_be.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
@Getter
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.api-url}")
    private String vnp_ApiUrl;

    // Generate unique transaction reference
    public String generateTxnRef() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return "PAY" + System.currentTimeMillis() + code;
    }

    // HMAC SHA512
    public String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKeySpec);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(bytes);
    }

    // Convert byte array to hex string
    private String bytesToHex(byte[] hash) {
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Validate signature
    public boolean validateSignature(Map<String, String> params, String receivedHash) {
        if (receivedHash == null || receivedHash.isEmpty()) {
            log.warn("Received hash is null or empty");
            return false;
        }

        try {
            // Create a copy to avoid modifying original params
            Map<String, String> paramsCopy = new HashMap<>(params);

            // Remove security fields from the copy
            paramsCopy.remove("vnp_SecureHash");
            paramsCopy.remove("vnp_SecureHashType");

            // Sort parameters alphabetically
            List<String> fieldNames = new ArrayList<>(paramsCopy.keySet());
            Collections.sort(fieldNames);

            // Build hash data string with sorted parameters
            StringBuilder hashData = new StringBuilder();
            boolean isFirst = true;

            for (String fieldName : fieldNames) {
                String fieldValue = paramsCopy.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (!isFirst) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName)
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    isFirst = false;
                }
            }

            // Calculate hash using vnp_HashSecret as the HMAC key
            String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());

            // Compare calculated hash with received hash (case-insensitive)
            return calculatedHash.equalsIgnoreCase(receivedHash);

        } catch (Exception e) {
            log.error("Error validating signature: {}", e.getMessage(), e);
            return false;
        }
    }
}