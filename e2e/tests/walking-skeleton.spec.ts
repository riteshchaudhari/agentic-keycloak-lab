import { expect, test } from '@playwright/test';

test('a logged-out visitor sees only a login button', async ({ page }) => {
  await page.goto('/');

  await expect(page.getByRole('button', { name: 'Log in' })).toBeVisible();
  await expect(page.getByTestId('products')).toHaveCount(0);
});

test('a Customer logs in via Keycloak and sees the Catalog', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Log in' }).click();
  await expect(page).toHaveURL(/localhost:8080\/realms\/shop\//);

  // Authorization Code + PKCE with S256 (ADR-0001).
  const authorizeUrl = new URL(page.url());
  expect(authorizeUrl.searchParams.get('response_type')).toBe('code');
  expect(authorizeUrl.searchParams.get('code_challenge_method')).toBe('S256');

  // The real Keycloak login page, not the shop app.
  await page.getByLabel('Username or email').fill('alice');
  await page.getByLabel('Password', { exact: true }).fill('alice');
  await page.getByRole('button', { name: 'Sign In' }).click();

  await expect(page.getByTestId('username')).toHaveText('alice');
  const products = page.getByTestId('products').getByRole('listitem');
  await expect(products).toHaveCount(5);
  await expect(products.first()).toContainText('Coffee Mug');
  await expect(products.first()).toContainText('€12.50');
});
