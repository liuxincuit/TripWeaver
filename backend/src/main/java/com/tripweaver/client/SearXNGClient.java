package com.tripweaver.client;

import com.tripweaver.config.SearXNGProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
public class SearXNGClient {

    private final SearXNGProperties properties;
    private final OkHttpClient httpClient;

    public SearXNGClient(SearXNGProperties properties) {
        this.properties = properties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String search(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = properties.getBaseUrl() + "/search?q=" + encodedQuery + "&format=json";

        for (int attempt = 1; attempt <= properties.getRetryCount(); attempt++) {
            try {
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().string();
                    }
                    log.warn("SearXNG request failed with status: {}", response.code());
                }
            } catch (Exception e) {
                log.warn("SearXNG request attempt {} failed: {}", attempt, e.getMessage());
            }

            if (attempt < properties.getRetryCount()) {
                try {
                    Thread.sleep(properties.getRetryDelayMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return "搜索服务暂时不可用，请稍后重试。";
    }
}
