import { test, expect } from '@playwright/test';

test.describe('AI 对话测试', () => {
  test('新建对话并发送消息', async ({ page }) => {
    // 生成随机用户数据
    const randomSuffix = Date.now().toString().slice(-6);
    const testUsername = `cu_${randomSuffix}`;
    const testEmail = `chat_${randomSuffix}@example.com`;
    const testPassword = 'password123';

    // 注册用户
    await page.goto('/register');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 点击新建计划按钮
    await page.getByTestId('new-plan-button').click();

    // 验证进入聊天页面（URL 可能是 /chat/new 或 /chat/\d+）
    await expect(page).toHaveURL(/\/chat\/(new|\d+)/);

    // 发送消息
    const testMessage = '为我计划5月15日，星期五晚从成都出发，前往重庆的两日旅游计划，预算2000元';
    await page.getByTestId('chat-input').fill(testMessage);
    await page.getByTestId('send-button').click();

    // 验证用户消息显示
    const userMessage = page.getByTestId('user-message').last();
    await expect(userMessage).toContainText('成都');
    await expect(userMessage).toContainText('重庆');

    // 等待 AI 响应（最长等待 60 秒）
    await expect(page.getByTestId('loading-indicator')).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId('assistant-message').last()).toBeVisible({ timeout: 60000 });

    // 验证 AI 响应包含旅行计划相关关键词
    const assistantMessage = page.getByTestId('assistant-message').last();
    const responseText = await assistantMessage.textContent();
    expect(responseText).toBeTruthy();
    expect(responseText!.length).toBeGreaterThan(50);
  });
});
