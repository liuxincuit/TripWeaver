package com.tripweaver;

import com.tripweaver.config.SearXNGProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SearXNGProperties.class)
public class TripWeaverApplication {

    public static void main(String[] args) {
        // Load .env from project root when running from backend/
        var dotenv = Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();

        // Set dotenv values as system properties for Spring to read
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(TripWeaverApplication.class, args);
    }
}