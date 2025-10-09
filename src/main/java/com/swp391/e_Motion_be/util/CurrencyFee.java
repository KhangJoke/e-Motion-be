package com.swp391.e_Motion_be.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFee {
    public static String toVND(double amount) {
        NumberFormat vnFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return vnFormat.format(amount) + "₫";
    }
}
