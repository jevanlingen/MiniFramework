package com.di.architecture;

import com.di.annotations.Configuration;

@Configuration
public class JsonMapper {
    private static final tools.jackson.databind.json.JsonMapper jsonMapper = tools.jackson.databind.json.JsonMapper.builder().build();

    public String writeValueAsString(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write value as string: " + e.getMessage(), e);
        }
    }

    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return jsonMapper.readValue(content, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize content: " + e.getMessage(), e);
        }
    }
}
