package com.swp391.e_Motion_be.dto.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class VehicleLogItemListConverter implements AttributeConverter<List<com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert VehicleLogItem list to JSON", e);
        }
    }

    @Override
    public List<com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to VehicleLogItem list", e);
        }
    }
}
