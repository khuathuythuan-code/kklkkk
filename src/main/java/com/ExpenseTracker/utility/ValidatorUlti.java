package com.ExpenseTracker.utility;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidatorUlti {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static boolean isUsernameValid(String username) {
        return username != null && !username.isEmpty() && username.length() <= 20;
    }

//    public static boolean isPasswordValid(String password) {
//        return password != null && password.length() >= 6;
//    }

    public static String isPasswordValid(String password) {
        if (password == null || password.length() < 6) {
            return "lengthError";
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

         if(!(hasUpper && hasLower && hasDigit && hasSpecial)){
             return "typoError";
         }
         return "noError";
    }


    public static boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhoneValid(String phoneStr) {
        if (phoneStr == null) return false;
        return phoneStr.matches("\\d{10,11}");
    }

}
