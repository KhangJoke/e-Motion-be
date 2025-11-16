package com.swp391.e_Motion_be.enums.payment;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    PENDING("Đang chờ"),
    REFUND("Hoàn tiền");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}


