package com.tripweaver.tools;

import com.tripweaver.client.SearXNGClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSearchToolTest {

    @Mock
    private SearXNGClient searxngClient;

    @InjectMocks
    private WebSearchTool webSearchTool;

    @Test
    void shouldCallClientWithQuery() {
        when(searxngClient.search("北京到上海高铁")).thenReturn("{\"results\": []}");

        String result = webSearchTool.search("北京到上海高铁");

        assertNotNull(result);
        verify(searxngClient).search("北京到上海高铁");
    }

    @Test
    void shouldReturnErrorMessageWhenClientFails() {
        when(searxngClient.search(anyString())).thenReturn("搜索服务暂时不可用，请稍后重试。");

        String result = webSearchTool.search("test");

        assertTrue(result.contains("搜索服务暂时不可用"));
    }
}
