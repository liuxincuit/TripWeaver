package com.tripweaver;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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