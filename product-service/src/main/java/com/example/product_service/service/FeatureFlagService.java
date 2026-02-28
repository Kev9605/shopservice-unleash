package com.example.product_service.service;

import io.getunleash.Unleash;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private final Unleash unleash;

    public FeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }

    public boolean premiumPricingEnabled() {
        try {
            return unleash.isEnabled("premium-pricing", false);
        } catch (Exception ex) {
            return false;
        }
    }
}
