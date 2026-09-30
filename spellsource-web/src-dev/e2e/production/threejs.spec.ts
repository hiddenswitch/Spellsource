import { expect, test } from '@playwright/test';

// Only the website session is supplied by the test. Matchmaking, subscriptions,
// mulligan and concession use a real guest account against the deployed gateway.
test('Three.js plays a bot match and returns to the deck picker', async ({ page, request }) => {
  const response = await request.post('https://graphql.playspellsource.com/graphql', {
    data: { query: `mutation { createAccount(input: {guest: true, decks: true, email: "smoke@spellsource.com", username: "smoke", password: "smoke"}) { accessToken { token } } }` },
  });
  expect(response.ok()).toBeTruthy();
  const account = await response.json();
  expect(account.errors).toBeUndefined();
  await page.route('**/api/auth/session', route => route.fulfill({ json: {
    user: { name: 'Smoke' }, expires: new Date(Date.now() + 3600000).toISOString(),
    token: { accessToken: account.data.createAccount.accessToken.token },
  } }));
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/game-3js');
  await page.getByRole('button', { name: 'Play vs Bot' }).click();
  await page.getByRole('button', { name: 'Confirm', exact: true }).click({ timeout: 45000 });
  await page.getByRole('button', { name: 'Concede', exact: true }).click({ timeout: 45000 });
  await page.getByRole('button', { name: 'Return', exact: true }).click({ timeout: 45000 });
  await expect(page.getByRole('button', { name: 'Play vs Bot' })).toBeEnabled();
  expect(errors).toEqual([]);
});
