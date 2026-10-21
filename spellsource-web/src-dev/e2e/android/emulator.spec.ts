import { expect, test, _android } from "@playwright/test";
import { execFileSync } from "node:child_process";
import { currentGameId, driveGuestIntoMatch, waitForClient, watchGraphQL } from "../production/client";

const serial = process.env.ANDROID_SERIAL ?? "emulator-5580";
const avd = process.env.ANDROID_AVD ?? "spellsource_api35";
const baseURL = process.env.SPELLSOURCE_BASE_URL ?? "https://playspellsource.com";
const adb = (...args: string[]) => execFileSync(process.env.ADB ?? "adb", ["-s", serial, ...args], { encoding: "utf8" }).trim();

test("Android Chrome restores a touch match across network, lifecycle, rotation and tab loss", async ({}, testInfo) => {
  expect(serial, "Use the dedicated emulator, never an attached physical device").toMatch(/^emulator-\d+$/);
  expect(adb("emu", "avd", "name").split(/\r?\n/)[0]).toBe(avd);
  const device = (await _android.devices({ omitDriverInstall: true })).find(d => d.serial() === serial);
  expect(device, `No emulator ${serial} found`).toBeTruthy();
  const context = await device!.launchBrowser({ baseURL, isMobile: true, hasTouch: true });
  await context.tracing.start({ screenshots: true, snapshots: true });
  let page = await context.newPage();
  try {
    // This is the dedicated test emulator: remove only this site's old test account.
    const cdp = await context.newCDPSession(page);
    await cdp.send("Storage.clearDataForOrigin", { origin: new URL(baseURL).origin, storageTypes: "all" });
    await cdp.detach();
    const graph = await watchGraphQL(page);
    await page.goto("/game");
    await waitForClient(page);
    if (process.env.SPELLSOURCE_EXPECTED_BUILD_ID) {
      await expect(page.locator("html")).toHaveAttribute("data-webgl-build", process.env.SPELLSOURCE_EXPECTED_BUILD_ID);
    }
    const { userId, token } = await driveGuestIntoMatch(page, graph, true);
    const gameId = await currentGameId(context.request, graph.origin, token);
    expect(gameId).toBeTruthy();
    const live = () => expect(page.locator("html")).toHaveAttribute("data-game-session", "live", { timeout: 30_000 });
    await live();
    await page.screenshot({ path: testInfo.outputPath("portrait.png") });

    await context.setOffline(true);
    await expect(page.getByTestId("game-recovery")).toBeVisible({ timeout: 12_000 });
    await context.setOffline(false);
    await live();
    expect(await currentGameId(context.request, graph.origin, token)).toBe(gameId);

    const backgroundAt = Date.now();
    adb("shell", "input", "keyevent", "KEYCODE_HOME");
    await page.waitForTimeout(10_000);
    adb("shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.LAUNCHER", "-n", "com.android.chrome/com.google.android.apps.chrome.Main");
    await live();
    await graph.waitFor("connectToGame", 30_000, Boolean, backgroundAt);
    // A fresh test AVD has no PIN. This does not unlock or alter any physical phone.
    const sleepAt = Date.now();
    adb("shell", "input", "keyevent", "KEYCODE_SLEEP");
    await page.waitForTimeout(10_000);
    adb("shell", "input", "keyevent", "KEYCODE_WAKEUP");
    adb("shell", "wm", "dismiss-keyguard");
    await graph.waitFor("connectToGame", 30_000, Boolean, sleepAt);
    await live();
    expect(await currentGameId(context.request, graph.origin, token)).toBe(gameId);

    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0");
    adb("shell", "settings", "put", "system", "user_rotation", "1");
    await expect.poll(() => page.evaluate(() => innerWidth > innerHeight)).toBe(true);
    await live();
    await page.screenshot({ path: testInfo.outputPath("landscape.png") });

    await page.close();
    page = await context.newPage();
    const restored = await watchGraphQL(page);
    await page.goto("/game");
    await waitForClient(page);
    expect((await restored.waitFor("account", 60_000)).id).toBe(userId);
    expect(await restored.waitFor("isInMatch", 60_000, Boolean)).toBe(gameId);
    await live();
    expect(restored.seen.some(event => event.key === "createAccount")).toBe(false);
  } finally {
    if (!page.isClosed()) await page.screenshot({ path: testInfo.outputPath("final.png") }).catch(() => {});
    await context.tracing.stop({ path: testInfo.outputPath("trace.zip") });
    adb("shell", "settings", "put", "system", "user_rotation", "0");
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "1");
    await context.close();
    await device!.close();
  }
});
