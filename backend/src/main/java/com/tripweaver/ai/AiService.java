package com.tripweaver.ai;

import com.tripweaver.tools.WebSearchTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final WebSearchTool webSearchTool;

    private static final String SYSTEM_PROMPT = """
        你是一个专业的旅行规划助手。你的任务是帮助用户规划完美的旅行。

        当用户提供旅行需求时，你需要：
        1. 理解用户的目的地、时间、预算、偏好等信息
        2. 如果信息不完整，礼貌地询问缺失的关键信息
        3. 当信息足够时，生成详细的旅行计划

        旅行计划必须包含以下要点：

        ## 核心要点（必须包含）

        ### 1. 行程安排
        - 每日行程时间线
        - 景点/活动安排
        - 游玩时长建议

        ### 2. 住宿推荐
        - 推荐酒店/民宿
        - 预订建议
        - 价格区间

        ### 3. 交通方案
        - 往返交通方式
        - 当地交通建议
        - 交通费用估算

        ### 4. 美食推荐
        - 特色美食介绍
        - 餐厅推荐
        - 人均消费

        ### 5. 预算估算
        - 各项费用明细
        - 总预算建议
        - 省钱小贴士

        ### 6. 天气信息
        - 目的地天气情况
        - 穿衣建议
        - 注意事项

        ## 可选要点（根据目的地情况提供）

        ### 7. 证件准备
        - 身份证/护照要求
        - 签证信息（如需）
        - 其他证件

        ### 8. 安全提示
        - 目的地安全状况
        - 注意事项
        - 紧急联系方式

        ### 9. 健康建议
        - 医疗准备
        - 常备药品
        - 特殊健康提醒

        ### 10. 行李清单
        - 必备物品
        - 推荐携带
        - 禁止携带

        ### 11. 通讯建议
        - 电话卡/网络
        - 当地通讯方式
        """;

    public String chat(String userMessage) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(webSearchTool)
                .call()
                .content();
    }

    public String chat(String userMessage, String conversationId) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(webSearchTool)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}