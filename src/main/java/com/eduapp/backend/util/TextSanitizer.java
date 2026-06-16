package com.eduapp.backend.util;

/**
 * SEC-6 — helpers for sanitizing short identity fields (names, school names) where HTML/markup
 * has no legitimate place. Removes angle brackets so a value can never become an HTML tag,
 * regardless of where it is later rendered (React, emails, PDFs). Do NOT use on rich content
 * such as question/answer text, which may legitimately contain {@code <}/{@code >} (maths).
 */
public final class TextSanitizer {

    private TextSanitizer() {
    }

    /** Strips {@code <} and {@code >} and trims; returns null for null input. */
    public static String stripAngleBrackets(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("<", "").replace(">", "").trim();
    }
}
