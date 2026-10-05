package ru.project.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {
    @Value("${app.timezone:UTC}")
    private String timeZone;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(timeZone));
    }
}
