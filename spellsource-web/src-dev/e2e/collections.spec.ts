import { expect, test } from "../playwright/fixtures";

test("should navigate to collection and see premade decks", async ({ page }) => {
  await page.goto("/home");
  await page.getByLabel("Collection").click();
  await page.waitForURL("/collection", { waitUntil: "domcontentloaded" });
  await page.waitForTimeout(10000); // TODO base this on active apollo queries / network requests
  const listCount = await page.getByLabel("Premade Deck List").locator("li").count();
  expect(listCount).toBeGreaterThan(0);
});
