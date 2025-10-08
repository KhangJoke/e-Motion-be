package com.swp391.e_Motion_be.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pricing.vehicle")
@Data
public class VehiclePricingConfig {
    private double price8hMultiplier;
    private double price12hMultiplier;
    private double priceDayMultiplier;
}
