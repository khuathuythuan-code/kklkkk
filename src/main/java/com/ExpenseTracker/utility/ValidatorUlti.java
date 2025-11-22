package com.ExpenseTracker.utility;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidatorUlti {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static boolean isUsernameValid(String username) {
        return username != null && !username.isEmpty() && username.length() <= 20;
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhoneValid(String phoneStr) {
        if (phoneStr == null) return false;
        return phoneStr.matches("\\d{10,11}");
    }

    public static boolean isDateOfBirthValid(LocalDate dob) {
        if (dob == null) return false;
        return !dob.isAfter(LocalDate.now());
    }
}
