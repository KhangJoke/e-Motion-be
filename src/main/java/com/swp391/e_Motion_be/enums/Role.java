package com.swp391.e_Motion_be.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Role {
    ROLE_ADMIN("Admin"), ROLE_USER("Khách hàng"), ROLE_STAFF("Nhân viên");

    private final String displayName;

    @JsonValue
    public String getName() {
        return displayName;
    }
}
