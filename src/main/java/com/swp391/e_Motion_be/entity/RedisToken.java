package com.swp391.e_Motion_be.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedisToken {
    private String jwtId;
    private Long expiredTime;
}
