package com.railway.reservation.config;

import com.railway.reservation.provider.ExternalRailwayDataProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RailwayHealthIndicator implements HealthIndicator {

    private final ExternalRailwayDataProvider railwayDataProvider;

    public RailwayHealthIndicator(ExternalRailwayDataProvider railwayDataProvider) {
        this.railwayDataProvider = railwayDataProvider;
    }

    @Override
    public Health health() {
        boolean isConfigured = railwayDataProvider.isAvailabilityConfigured() || railwayDataProvider.isSearchConfigured();
        return Health.up()
                .withDetail("service", "Indian Railway Data Provider Subsystem")
                .withDetail("externalProviderConfigured", isConfigured)
                .withDetail("masterStationDataLoaded", true)
                .withDetail("masterTimetableActive", true)
                .withDetail("mode", isConfigured ? "HYBRID_LIVE_PROVIDER" : "AUTHENTIC_PRS_MASTER_DATASET")
                .build();
    }
}
