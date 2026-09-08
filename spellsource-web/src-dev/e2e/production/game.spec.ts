import { expect, test } from "@playwright/test";
import { graphqlHosts } from "./hosts";

test.describe("browser client", () => {
  test("loads with visible progress and connects its subscriptions socket", async ({ page }) => {
    const pageErrors: string[] = [];
    page.on("pageerror", (e) => pageErrors.push(String(e)));

    const socketHosts: string[] = [];
    page.on("websocket", (ws) => socketHosts.push(new URL(ws.url()).hostname));

    // throttle so the overlay is observable; unthrottled the client is up in seconds
    const cdp = await page.context().newCDPSession(page);
    await cdp.send("Network.enable");
    await cdp.send("Network.emulateNetworkConditions", {
      offline: false,
      latency: 20,
      downloadThroughput: 3_000_000,
      uploadThroughput: 3_000_000,
    });

    await page.goto("/game");
    const progress = page.getByRole("progressbar");
    await expect(progress).toBeVisible();
    await expect
      .poll(async () => Number(await progress.getAttribute("aria-valuenow")), { timeout: 60_000 })
      .toBeGreaterThan(0);

    // the overlay leaves once the engine is running
    await expect(progress).toBeHidden({ timeout: 120_000 });
    await expect(page.locator("#unity-canvas")).toBeVisible();
    expect(pageErrors).toEqual([]);

    // Play as Guest sits centred in the account panel; entering the menu opens
    // the client's graphql-ws connection, which must go to a host that exists
    const box = page.viewportSize()!;
    await page.mouse.click(box.width * 0.499, box.height * 0.798);
    await expect.poll(() => socketHosts.length, { timeout: 60_000 }).toBeGreaterThan(0);
    for (const host of socketHosts) {
      expect(graphqlHosts, `client opened a socket to ${host}`).toContain(host);
    }
  });
});
