package com.swp391.e_Motion_be.enums.payment;

import lombok.Getter;

@Getter
public enum PaymentType {

    RESERVATION("Đặt cọc giữ xe"),
    RENTAL("Thanh toán khi thuê xe"),
    PENALTY_FEE_RENTAL("Phí phạt & chi phí phát sinh"),
    REFUND("Hoàn tiền"),
    TEST("Thanh toán thử nghiệm"),
    RENTAL_EXTENSION("Gia hạn thuê xe");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

}
