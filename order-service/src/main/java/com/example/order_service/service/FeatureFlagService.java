package com.example.order_service.service;

import io.getunleash.Unleash;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private final Unleash unleash;

    public FeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }

    public boolean orderNotificationsEnabled() {
        try {
            return unleash.isEnabled("order-notifications", false);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean bulkOrderDiscountEnabled() {
        try {
            return unleash.isEnabled("bulk-order-discount", false);
        } catch (Exception ex) {
            return false;
        }
    }
}
