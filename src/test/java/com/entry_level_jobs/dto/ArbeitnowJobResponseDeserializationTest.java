package com.entry_level_jobs.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ArbeitnowJobResponseDeserializationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDeserializeEpochSeconds() throws Exception {
        String json = "{ \"data\": [ { \"title\": \"Dev\", \"company_name\": \"ACME\", \"created_at\": 1700000000 } ] }";
        ArbeitnowJobResponse resp = mapper.readValue(json, ArbeitnowJobResponse.class);
        assertNotNull(resp.getData());
        assertEquals(1, resp.getData().size());
        assertEquals(Long.valueOf(1700000000L), resp.getData().get(0).getCreated_at());
    }

    @Test
    public void testDeserializeEpochMillisString() throws Exception {
        String json = "{ \"data\": [ { \"title\": \"Dev\", \"company_name\": \"ACME\", \"created_at\": \"1700000000000\" } ] }";
        ArbeitnowJobResponse resp = mapper.readValue(json, ArbeitnowJobResponse.class);
        assertNotNull(resp.getData());
        assertEquals(Long.valueOf(1700000000L), resp.getData().get(0).getCreated_at());
    }

    @Test
    public void testDeserializeIsoString() throws Exception {
        String iso = Instant.ofEpochSecond(1700000000L).toString();
        String json = "{ \"data\": [ { \"title\": \"Dev\", \"company_name\": \"ACME\", \"created_at\": \"" + iso + "\" } ] }";
        ArbeitnowJobResponse resp = mapper.readValue(json, ArbeitnowJobResponse.class);
        assertNotNull(resp.getData());
        assertEquals(Long.valueOf(1700000000L), resp.getData().get(0).getCreated_at());
    }
}

