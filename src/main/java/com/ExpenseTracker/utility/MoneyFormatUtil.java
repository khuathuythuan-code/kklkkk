package com.ExpenseTracker.utility;

import com.ExpenseTracker.Singleton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class MoneyFormatUtil {

    private static Locale getLocale() {
        String lang = Singleton.getInstance().currentLanguage;
        if (lang.equalsIgnoreCase("vi"))
            return new Locale("vi", "VN");
        return Locale.US;
    }

    public static String format(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(getLocale());
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        return nf.format(amount);
    }

    public static double parse(String formatted) {
        if (formatted == null || formatted.isBlank())
            return 0;

        Locale locale = getLocale();
        NumberFormat nf = NumberFormat.getNumberInstance(locale);

        try {
            return nf.parse(formatted).doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }
}