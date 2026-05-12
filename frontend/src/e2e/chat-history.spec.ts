import { test, expect } from '@playwright/test';

test.describe('聊天历史持久化', () => {
  // 测试新建计划时显示欢迎消息
  test('新建计划显示欢迎消息', async ({ page }) => {
    // 生成随机用户数据
    const randomSuffix = Date.now().toString().slice(-6);
    const testUsername = `ch_${randomSuffix}`;
    const testEmail = `ch_${randomSuffix}@example.com`;
    const testPassword = 'password123';

    // 注册用户
    await page.goto('/register');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 新建计划
    await page.getByTestId('new-plan-button').click();

    // 等待 URL 更新为具体的 planId（不是 'new'）
    await expect(page).toHaveURL(/\/chat\/\d+/, { timeout: 10000 });

    // 验证显示欢迎消息
    await expect(page.getByTestId('assistant-message').first()).toBeVisible({ timeout: 10000 });
    const assistantMessageCount = await page.getByTestId('assistant-message').count();
    expect(assistantMessageCount).toBe(1);

    // 验证没有用户消息
    const userMessageCount = await page.getByTestId('user-message').count();
    expect(userMessageCount).toBe(0);
  });

  // 测试发送消息后消息被正确显示
  test('发送消息后消息正确显示', async ({ page }) => {
    // 生成随机用户数据
    const randomSuffix = Date.now().toString().slice(-6);
    const testUsername = `ch2_${randomSuffix}`;
    const testEmail = `ch2_${randomSuffix}@example.com`;
    const testPassword = 'password123';

    // 注册用户
    await page.goto('/register');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 新建计划
    await page.getByTestId('new-plan-button').click();
    await expect(page).toHaveURL(/\/chat\/\d+/, { timeout: 10000 });

    // 等待欢迎消息
    await expect(page.getByTestId('assistant-message').first()).toBeVisible({ timeout: 10000 });

    // 发送消息
    await page.getByTestId('chat-input').fill('帮我规划上海两日游');
    await page.getByTestId('send-button').click();

    // 验证用户消息显示
    await expect(page.getByTestId('user-message').first()).toBeVisible({ timeout: 5000 });
    const userMessageCount = await page.getByTestId('user-message').count();
    expect(userMessageCount).toBe(1);

    // 等待 AI 响应（最长等待 60 秒）
    await expect(page.getByTestId('assistant-message').last()).toBeVisible({ timeout: 60000 });

    // 记录当前消息数量
    const userCount = await page.getByTestId('user-message').count();
    const assistantCount = await page.getByTestId('assistant-message').count();

    // 刷新页面
    await page.reload();

    // 验证历史消息恢复
    await expect(page.getByTestId('assistant-message').first()).toBeVisible({ timeout: 10000 });
    const restoredUserCount = await page.getByTestId('user-message').count();
    const restoredAssistantCount = await page.getByTestId('assistant-message').count();

    // 验证消息数量一致
    expect(restoredUserCount).toBe(userCount);
    expect(restoredAssistantCount).toBe(assistantCount);
  });
});
