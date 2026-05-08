package com.tripweaver.tools;

import com.tripweaver.client.SearXNGClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSearchTool {

    private final SearXNGClient searxngClient;

    @Tool(description = """
        搜索互联网获取实时信息，如交通班次、票价、景点开放时间、酒店价格等。
        当用户询问需要实时数据的问题时使用此工具。
        """)
    public String search(@ToolParam(description = "搜索查询关键词") String query) {
        return searxngClient.search(query);
    }
}
