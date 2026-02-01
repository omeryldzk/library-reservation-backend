package com.example.libraryweek1.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Getter
@Configuration
@ConfigurationProperties(prefix = "app.datetime")
@PropertySource("classpath:application.yml")
public class DateTimeConfig {
    private ZoneId timeZone = ZoneId.systemDefault(); // Default: System timezone
    private Locale locale = Locale.getDefault();      // Default: System locale

    public void setTimeZone(String timeZone) {
        this.timeZone = ZoneId.of(timeZone); // Converts String to ZoneId
    }

    @PostConstruct
    public void init() {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));

        System.out.println("Date in UTC: " + new Date().toString());
    }

    public void setLocale(String locale) {
        this.locale = Locale.forLanguageTag(locale);
    }
}