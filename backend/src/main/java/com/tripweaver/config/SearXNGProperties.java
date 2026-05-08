package com.tripweaver.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "searxng")
public class SearXNGProperties {

    @NotBlank(message = "SearXNG base URL is required")
    private String baseUrl;

    private int retryCount;

    private int retryDelayMs;
}
