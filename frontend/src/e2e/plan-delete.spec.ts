import { test, expect } from '@playwright/test';

test.describe('计划删除级联清理', () => {
  test('删除计划后聊天历史被清理', async ({ page }) => {
    const randomSuffix = Date.now().toString().slice(-6);
    const testUsername = `del_${randomSuffix}`;
    const testEmail = `del_${randomSuffix}@example.com`;
    const testPassword = 'password123';

    await page.goto('/register');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    await page.getByTestId('new-plan-button').click();
    await expect(page).toHaveURL(/\/chat\/\d+/, { timeout: 10000 });
    await expect(page.getByTestId('assistant-message').first()).toBeVisible({ timeout: 10000 });

    await page.getByTestId('chat-input').fill('帮我规划北京三日游');
    await page.getByTestId('send-button').click();
    await expect(page.getByTestId('user-message').first()).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId('assistant-message').last()).toBeVisible({ timeout: 60000 });

    const planId = page.url().match(/\/chat\/(\d+)/)?.[1];
    expect(planId).toBeTruthy();

    await page.waitForTimeout(1000);

    await page.goto('/plans');
    await expect(page.getByTestId(`menu-btn-${planId}`)).toBeVisible({ timeout: 10000 });
    
    const planCountBefore = await page.getByTestId(/menu-btn-/).count();
    
    await page.getByTestId(`menu-btn-${planId}`).click();
    await expect(page.getByTestId('delete-plan-btn')).toBeVisible();

    const deletePromise = page.waitForResponse(resp => 
      resp.url().includes('/api/plans/') && resp.request().method() === 'DELETE'
    );
    
    page.on('dialog', async dialog => {
      console.log('Dialog message:', dialog.message());
      await dialog.accept();
    });
    await page.getByTestId('delete-plan-btn').click();
    await deletePromise;
    
    await page.waitForTimeout(1000);
    
    const planCountAfter = await page.getByTestId(/menu-btn-/).count();
    expect(planCountAfter).toBe(planCountBefore - 1);
  });
});
