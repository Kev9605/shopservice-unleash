package com.example.product_service.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.FakeUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnleashConfigBean {

    @Bean
    public Unleash unleash(
            @Value("${unleash.app-name}") String appName,
            @Value("${unleash.instance-id}") String instanceId,
            @Value("${unleash.api-url}") String apiUrl,
            @Value("${unleash.api-token}") String apiToken
    ) {
        try{
            UnleashConfig config = UnleashConfig.builder()
                    .appName(appName)
                    .instanceId(instanceId)
                    .unleashAPI(apiUrl)
                    .apiKey(apiToken == null ? "" : apiToken)
                    .build();

            return new DefaultUnleash(config);
        } catch (Exception e) {
            // fallback behavior when Unleash is unavailable
            return new FakeUnleash();
        }
    }
}
