import { test, expect } from '@playwright/test';

test.describe('认证流程测试', () => {
  test('用户注册成功', async ({ page }) => {
    // 生成随机用户数据（用户名限制3-20字符）
    const randomSuffix = Date.now().toString().slice(-6);
    const testUsername = `tu_${randomSuffix}`;
    const testEmail = `test_${randomSuffix}@example.com`;
    const testPassword = 'password123';

    // 访问注册页面
    await page.goto('/register');

    // 填写注册表单
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);

    // 点击注册按钮
    await page.getByTestId('register-button').click();

    // 验证跳转到首页
    await expect(page).toHaveURL('/');

    // 验证首页显示新建计划按钮（表示已登录）
    await expect(page.getByTestId('new-plan-button')).toBeVisible();
  });

  test('用户登录成功', async ({ page }) => {
    // 先注册用户
    const randomSuffix = Date.now().toString().slice(-6);
    const testUsername = `login_${randomSuffix}`;
    const testEmail = `login_${randomSuffix}@example.com`;
    const testPassword = 'password123';

    // 注册用户
    await page.goto('/register');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    // 登出（清除登录状态）
    await page.context().clearCookies();

    // 访问登录页面
    await page.goto('/login');

    // 填写登录表单
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('password-input').fill(testPassword);

    // 点击登录按钮
    await page.getByTestId('login-button').click();

    // 验证跳转到首页
    await expect(page).toHaveURL('/');

    // 验证首页显示新建计划按钮
    await expect(page.getByTestId('new-plan-button')).toBeVisible();
  });

  test('登录失败显示错误信息', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');

    // 填写错误的凭据
    await page.getByTestId('username-input').fill('nonexistent_user');
    await page.getByTestId('password-input').fill('wrongpassword');

    // 点击登录按钮
    await page.getByTestId('login-button').click();

    // 验证显示错误信息
    await expect(page.getByTestId('error-message')).toBeVisible();

    // 验证仍在登录页面
    await expect(page).toHaveURL('/login');
  });
});
