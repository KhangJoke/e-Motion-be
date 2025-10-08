package com.swp391.e_Motion_be.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {

    public static String formatVnCurrency(Double amount) {
        if (amount == null) return null;
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return vnFormat.format(amount);
    }
}
