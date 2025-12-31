package com.entry_level_jobs.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Deserializer that accepts either a numeric epoch seconds value or an ISO-8601 timestamp string
 * and converts it to a Long epoch seconds value.
 */
public class CreatedAtDeserializer extends JsonDeserializer<Long> {
    // threshold to distinguish seconds vs milliseconds (timestamps in ms will be much larger)
    private static final long MS_THRESHOLD = 10_000_000_000L; // ~Sat Nov 20 2286 if seconds; so >10B indicates milliseconds

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            long val = p.getLongValue();
            if (val > MS_THRESHOLD) {
                return val / 1000L;
            }
            return val;
        }

        if (token == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (text.isEmpty()) return null;
            try {
                // Try parsing as a long first
                long parsed = Long.parseLong(text);
                if (parsed > MS_THRESHOLD) {
                    return parsed / 1000L;
                }
                return parsed;
            } catch (NumberFormatException ignored) {
            }

            try {
                Instant instant = Instant.parse(text);
                return instant.getEpochSecond();
            } catch (DateTimeParseException ex) {
                // Give up and return null
                return null;
            }
        }

        return null;
    }
}
