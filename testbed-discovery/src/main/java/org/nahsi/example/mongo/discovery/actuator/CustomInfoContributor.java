package org.nahsi.example.mongo.discovery.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {

    @Value("${spring.application.name}")
    String name;

    @Value("${spring.profiles.active}")
    String profile;

    private final Instant now = Instant.now(); // unix time

    @Override
    public void contribute(Info.Builder builder) {

        Map<String, String> applicationInfo = new HashMap<>();
        applicationInfo.put("name", name);
        applicationInfo.put("startedAt", now.toString());
        applicationInfo.put("profile", profile);
        builder.withDetail("application", applicationInfo);

    }
}