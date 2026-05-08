package com.tripweaver.client;

import com.tripweaver.config.SearXNGProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SearXNGClientTest {

    private MockWebServer mockWebServer;
    private SearXNGClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        SearXNGProperties properties = new SearXNGProperties();
        properties.setBaseUrl(mockWebServer.url("/").toString());
        properties.setRetryCount(2);
        properties.setRetryDelayMs(100);

        client = new SearXNGClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnSearchResults() {
        String mockResponse = """
            {
              "results": [
                {"title": "北京到上海高铁", "url": "https://example.com/1", "content": "高铁班次信息..."}
              ]
            }
            """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String result = client.search("北京到上海高铁");

        assertNotNull(result);
        assertTrue(result.contains("北京到上海高铁"));
    }

    @Test
    void shouldRetryOnFailure() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"results\": []}")
                .addHeader("Content-Type", "application/json"));

        String result = client.search("test query");

        assertNotNull(result);
    }

    @Test
    void shouldReturnErrorMessageAfterMaxRetries() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        String result = client.search("test query");

        assertTrue(result.contains("搜索服务暂时不可用"));
    }
}
