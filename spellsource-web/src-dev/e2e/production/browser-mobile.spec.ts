import { expect, test } from "@playwright/test";
import { currentGameId, driveGuestIntoMatch, waitForClient, watchGraphQL } from "./client";

// Telemetry availability is independent of player startup and can emit WebKit
// page errors when the external analytics endpoint rejects cross-origin requests.
test.beforeEach(async ({ page }) => {
  await page.route("https://cdp.cloud.unity3d.com/**", route => route.fulfill({
    status: 204, headers: { "access-control-allow-origin": "*" },
  }));
});

test("the exact candidate starts at the device viewport", async ({ page }) => {
  const errors: string[] = [];
  page.on("pageerror", error => errors.push(error.message));
  await page.goto("/game");
  await waitForClient(page);
  await expect(page.getByText("Spellsource could not continue.")).toBeHidden();
  await expect(page.locator("canvas")).toBeVisible();
  if (process.env.SPELLSOURCE_EXPECTED_BUILD_ID) {
    await expect(page.locator("html")).toHaveAttribute("data-webgl-build", process.env.SPELLSOURCE_EXPECTED_BUILD_ID);
  }
  const canvas = await page.locator("canvas").evaluate(c => ({ width: (c as HTMLCanvasElement).width, css: c.clientWidth }));
  expect(canvas.width).toBeLessThanOrEqual(canvas.css * 2 + 1);
  expect(errors).toEqual([]);
});

test("a touch session recovers offline and after closing the tab", async ({ page, context, request, isMobile }) => {
  test.skip(!isMobile, "Desktop recovery is covered separately");
  test.setTimeout(300_000);
  const graphql = await watchGraphQL(page);
  await page.goto("/game");
  await waitForClient(page);
  const { userId, token } = await driveGuestIntoMatch(page, graphql, true);
  const gameId = await currentGameId(request, graphql.origin, token);
  expect(gameId).toBeTruthy();
  await expect(page.locator("html")).toHaveAttribute("data-game-session", "live", { timeout: 30_000 });

  await context.setOffline(true);
  await expect(page.getByTestId("game-recovery")).toBeVisible({ timeout: 12_000 });
  await expect(page.locator("html")).toHaveAttribute("data-game-session", "reconnecting");
  graphql.clear();
  await context.setOffline(false);
  await graphql.waitFor("connectToGame", 30_000, value => value === true);
  await expect(page.locator("html")).toHaveAttribute("data-game-session", "live", { timeout: 30_000 });
  expect(await currentGameId(request, graphql.origin, token)).toBe(gameId);

  await page.close();
  const reopened = await context.newPage();
  const restored = await watchGraphQL(reopened);
  await reopened.goto("/game");
  await waitForClient(reopened);
  expect((await restored.waitFor("account", 60_000)).id).toBe(userId);
  expect(await restored.waitFor("isInMatch", 60_000, Boolean)).toBe(gameId);
  await expect(reopened.locator("html")).toHaveAttribute("data-game-session", "live", { timeout: 30_000 });
  expect(restored.seen.some(event => event.key === "createAccount")).toBe(false);
});

test("desktop hand expands when it fits and tucks away after resizing", async ({ page, isMobile }, testInfo) => {
  test.skip(isMobile, "Mouse hand layout; touch keeps its existing tray");
  test.setTimeout(300_000);
  const graphql = await watchGraphQL(page);
  await page.goto("/game");
  await waitForClient(page);
  await driveGuestIntoMatch(page, graphql);
  // Keep the initial cards. The button sits 64 canvas units above the bottom edge.
  const canvas = page.locator("canvas");
  const size = await canvas.evaluate(c => ({ width: (c as HTMLCanvasElement).width, height: (c as HTMLCanvasElement).height }));
  const bounds = (await canvas.boundingBox())!;
  const scale = Math.min(2, size.width / 400, size.height / 400);
  await page.mouse.click(bounds.x + bounds.width / 2, bounds.y + bounds.height * (1 - 64 * scale / size.height));
  await page.mouse.move(0, 0);

  const layout = () => page.evaluate(() => (window as unknown as {
    __spellsourceHandLayout?: { count: number; expanded: boolean; bottom: number; top: number;
      left: number; right: number; width: number; height: number };
  }).__spellsourceHandLayout);
  await expect.poll(async () => (await layout())?.count ?? 0, { timeout: 30_000 }).toBeGreaterThan(0);

  for (const viewport of [{ width: 2560, height: 1440 }, { width: 960, height: 600 }, { width: 2560, height: 1440 }]) {
    await page.setViewportSize(viewport);
    const expanded = viewport.width === 2560;
    await expect.poll(async () => {
      const value = await layout();
      return !!value && value.width === viewport.width && value.height === viewport.height &&
        value.expanded === expanded && (expanded
          ? value.bottom >= 0 && value.top <= value.height && value.left >= 0 && value.right <= value.width
          : value.bottom < 0);
    }, { timeout: 20_000 }).toBe(true);
    await testInfo.attach(`hand-${viewport.width}x${viewport.height}`, {
      body: await page.screenshot(), contentType: "image/png",
    });
  }
});
