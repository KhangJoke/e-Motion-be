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

    public String getVnp_TmnCode() {
        return vnp_TmnCode;
    }

    public String getVnp_HashSecret() {
        return vnp_HashSecret;
    }

    public String getVnp_PayUrl() {
        return vnp_PayUrl;
    }

    public String getVnp_ReturnUrl() {return vnp_ReturnUrl;}

    public String getVnp_ApiUrl() {return vnp_ApiUrl;}

    public String generateTxnRef() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 6 chữ số random
        return "PAY" + System.currentTimeMillis() + code;
    }

    public String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKeySpec);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(bytes);
    }

    public String bytesToHex(byte[] hash) {
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public boolean validateSignature(Map<String, String> params, String vnp_SecureHash) {
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            log.warn("Received hash is null or empty");
            return false;
        }

        try {
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);

                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String calculatedHash = hmacSHA512(vnp_SecureHash, hashData.toString());
            boolean isValid = calculatedHash.equalsIgnoreCase(vnp_SecureHash);

            if (!isValid) {
                log.warn("Signature validation failed");
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating signature: {}", e.getMessage(), e);
            return false;
        }
    }
}

