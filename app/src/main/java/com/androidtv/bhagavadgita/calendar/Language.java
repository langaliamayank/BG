package com.androidtv.bhagavadgita.calendar;

public enum Language {
    ENGLISH, HINDI, GUJARATI;

    public static Language fromString(String name, Language fallback) {
        if (name == null) return fallback;
        try {
            return Language.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}