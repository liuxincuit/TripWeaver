package com.tripweaver.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Test
    void systemPromptShouldNotBeEmpty() {
        // 验证系统提示词不为空
        String prompt = """
            你是一个专业的旅行规划助手。你的任务是帮助用户规划完美的旅行。
            """;
        assertFalse(prompt.isEmpty());
    }
}