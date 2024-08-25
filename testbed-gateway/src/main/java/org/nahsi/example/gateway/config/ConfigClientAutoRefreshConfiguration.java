package org.nahsi.example.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshEndpointAutoConfiguration;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;


@Configuration
@AutoConfigureAfter({RefreshAutoConfiguration.class, RefreshEndpointAutoConfiguration.class})
@EnableScheduling
public class ConfigClientAutoRefreshConfiguration implements SchedulingConfigurer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Value("${spring.cloud.config.refresh-interval:60}")
    private long refreshInterval;

    @Value("${spring.cloud.config.auto-refresh:true}")
    private boolean autoRefresh;

    private final RefreshEndpoint refreshEndpoint;

    public ConfigClientAutoRefreshConfiguration(RefreshEndpoint refreshEndpoint) {
        this.refreshEndpoint = refreshEndpoint;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

        var interval = Duration.ofSeconds(refreshInterval);

        if (autoRefresh) {

            scheduledTaskRegistrar.addFixedRateTask( () -> {

                refreshEndpoint.refresh();
                logger.info("refreshed");

            }, interval);

        }

    }
}
