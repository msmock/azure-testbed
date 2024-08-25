package org.nahsi.example.test.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Runs a scheduled task which writes a log entry. Used to verify if the service is up and running.
 */
@Component
@EnableScheduling
public class PeriodicTaskScheduler implements SchedulingConfigurer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

        var interval = Duration.ofMinutes(1);
        scheduledTaskRegistrar.addFixedRateTask( () -> logger.debug("Run task ..."), interval);

    }

}