package com.btbs.domain.shared.value;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public final class PhoneNumber implements Serializable {
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    /**
     * Factory method for creating a phone number from a raw input.
     * Performs basic validation and normalization.
     */
    public static PhoneNumber of(String raw) {
        Objects.requireNonNull(raw, "phone number cannot be null");
        String normalized = normalize(raw);
        if (!E164_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + raw);
        }
        return new PhoneNumber(normalized);
    }

    private static String normalize(String input) {
        String trimmed = input.trim().replaceAll("\\s+", "");
        // Normalize to E.164-style string if it starts with "00"
        if (trimmed.startsWith("00")) {
            trimmed = "+" + trimmed.substring(2);
        }
        return trimmed;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;
        PhoneNumber that = (PhoneNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
