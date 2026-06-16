package com.eduapp.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * SEC-6 — global input hygiene: strips ISO control characters and null bytes (except tab,
 * newline, carriage-return) from every incoming JSON string. These are never legitimate in this
 * app's text and are a real vector for null-byte and log-injection attacks.
 *
 * <p>Deliberately does NOT strip {@code <}/{@code >} or other printable characters — question and
 * answer text legitimately contain maths (e.g. {@code x < y}); HTML/script safety is provided by
 * output encoding (React escapes by default; no raw-HTML render). Identity fields (names) get
 * extra angle-bracket stripping via {@link com.eduapp.backend.util.TextSanitizer}.
 */
public class SanitizingStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        value.codePoints().forEach(cp -> {
            if (cp == '\t' || cp == '\n' || cp == '\r' || !Character.isISOControl(cp)) {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }
}
