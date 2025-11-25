package com.ExpenseTracker.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

public class LanguageManagerUlti {
    private static Map<String, String> langMap;
    private static JsonNode langRoot;


    public static void setLocale(String lang) {
        String path = "/lang/" + lang + ".json";
        try (InputStream is = LanguageManagerUlti.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("File not found: " + path);
            ObjectMapper mapper = new ObjectMapper();
            langMap = mapper.readValue(is, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Lấy string, fallback về key nếu không có
    public static String get(String key) {
        if (langMap == null) return key;
        return langMap.getOrDefault(key, key);
    }

    // ✅ Lấy string, fallback về defaultValue nếu không có
    public static String get(String key, String defaultValue) {
        if (langMap == null) return defaultValue;
        return langMap.getOrDefault(key, defaultValue);
    }


}
