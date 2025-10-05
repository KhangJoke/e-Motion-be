package com.swp391.e_Motion_be.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;

import java.io.IOException;

public class VehicleStatusDeserializer extends JsonDeserializer<VehicleStatus> {
    @Override
    public VehicleStatus deserialize(JsonParser p, DeserializationContext ctxt){
        String value;
        try {
            value = p.getText();
        } catch (IOException e) {
            throw new AppException(ErrorCode.VEHICLE_STATUS_INVALID);
        }
        return VehicleStatus.fromDescription(value);
    }
}
