import { test, expect } from '@playwright/test';

test.describe('聊天历史权限安全测试', () => {
  test('用户不应能访问其他用户的对话历史', async ({ page }) => {
    // ===== 用户 A 注册并创建计划 =====
    const randomSuffixA = Date.now().toString().slice(-6);
    const usernameA = `user_a_${randomSuffixA}`;
    const emailA = `user_a_${randomSuffixA}@example.com`;
    const passwordA = 'password123';

    // 注册用户 A
    await page.goto('/register');
    await page.getByTestId('username-input').fill(usernameA);
    await page.getByTestId('email-input').fill(emailA);
    await page.getByTestId('password-input').fill(passwordA);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 用户 A 创建新计划并发送消息
    await page.getByTestId('new-plan-button').click();
    await expect(page).toHaveURL(/\/chat\/\d+/);

    // 从 URL 提取 planId
    const urlA = page.url();
    const planIdMatch = urlA.match(/\/chat\/(\d+)/);
    const planId = planIdMatch ? planIdMatch[1] : null;
    expect(planId).toBeTruthy();

    // 用户 A 发送一条消息
    const secretMessage = `这是用户A的秘密消息_${randomSuffixA}`;
    await page.getByTestId('chat-input').fill(secretMessage);
    await page.getByTestId('send-button').click();

    // 等待消息发送完成
    await expect(page.getByTestId('user-message').last()).toContainText('秘密消息', { timeout: 10000 });

    // 获取用户 A 的 token
    const tokenA = await page.evaluate(() => localStorage.getItem('token'));

    // 用户 A 登出
    await page.goto('/');
    await page.evaluate(() => localStorage.clear());

    // ===== 用户 B 注册 =====
    const randomSuffixB = (Date.now() + 1000).toString().slice(-6);
    const usernameB = `user_b_${randomSuffixB}`;
    const emailB = `user_b_${randomSuffixB}@example.com`;
    const passwordB = 'password123';

    await page.goto('/register');
    await page.getByTestId('username-input').fill(usernameB);
    await page.getByTestId('email-input').fill(emailB);
    await page.getByTestId('password-input').fill(passwordB);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 获取用户 B 的 token
    const tokenB = await page.evaluate(() => localStorage.getItem('token'));

    // ===== 用户 B 尝试访问用户 A 的对话历史 =====
    // 使用用户 B 的 token 访问用户 A 的 planId
    const historyResponse = await page.request.get(`/api/chat/history/${planId}`, {
      headers: {
        'Authorization': `Bearer ${tokenB}`
      }
    });

    // 安全检查：应该返回 403 Forbidden 或 404 Not Found
    // 如果返回 200，说明存在安全漏洞
    const status = historyResponse.status();

    if (status === 200) {
      // 如果能获取到历史，检查是否包含用户 A 的秘密消息
      const history = await historyResponse.json();
      const containsSecretMessage = history.some(
        msg => msg.content && msg.content.includes(secretMessage)
      );

      // 这个断言应该失败，证明安全漏洞存在
      expect(containsSecretMessage).toBe(false);
    } else {
      // 预期行为：应该拒绝访问
      expect([403, 404, 401]).toContain(status);
    }
  });

  test('用户不应能向其他用户的计划发送消息', async ({ page }) => {
    // ===== 用户 A 注册并创建计划 =====
    const randomSuffixA = Date.now().toString().slice(-6);
    const usernameA = `user_a2_${randomSuffixA}`;
    const emailA = `user_a2_${randomSuffixA}@example.com`;
    const passwordA = 'password123';

    await page.goto('/register');
    await page.getByTestId('username-input').fill(usernameA);
    await page.getByTestId('email-input').fill(emailA);
    await page.getByTestId('password-input').fill(passwordA);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    await page.getByTestId('new-plan-button').click();
    await expect(page).toHaveURL(/\/chat\/\d+/);

    const urlA = page.url();
    const planIdMatch = urlA.match(/\/chat\/(\d+)/);
    const planId = planIdMatch ? planIdMatch[1] : null;
    expect(planId).toBeTruthy();

    // 用户 A 登出
    await page.goto('/');
    await page.evaluate(() => localStorage.clear());

    // ===== 用户 B 注册 =====
    const randomSuffixB = (Date.now() + 1000).toString().slice(-6);
    const usernameB = `user_b2_${randomSuffixB}`;
    const emailB = `user_b2_${randomSuffixB}@example.com`;
    const passwordB = 'password123';

    await page.goto('/register');
    await page.getByTestId('username-input').fill(usernameB);
    await page.getByTestId('email-input').fill(emailB);
    await page.getByTestId('password-input').fill(passwordB);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 获取用户 B 的 token
    const tokenB = await page.evaluate(() => localStorage.getItem('token'));

    // ===== 用户 B 尝试向用户 A 的计划发送消息 =====
    const maliciousMessage = `恶意消息_${randomSuffixB}`;
    const sendResponse = await page.request.post('/api/chat/send', {
      headers: {
        'Authorization': `Bearer ${tokenB}`,
        'Content-Type': 'application/json'
      },
      data: { planId: parseInt(planId), message: maliciousMessage }
    });

    const status = sendResponse.status();

    if (status === 200) {
      // 如果发送成功，说明存在安全漏洞
      // 消息会被添加到用户 A 的对话历史中
      expect(status).not.toBe(200);
    } else {
      // 预期行为：应该拒绝访问
      expect([403, 404, 401]).toContain(status);
    }
  });
});
