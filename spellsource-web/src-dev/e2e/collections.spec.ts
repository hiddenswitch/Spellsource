import { expect, test } from "../playwright/fixtures";

test("should navigate to collection and see premade decks", async ({ page }) => {
  await page.goto("/home");
  await page.getByLabel("Collection").click();
  // await page.waitForURL("/collection", { waitUntil: "networkidle" });
  await page.waitForURL("/collection", { waitUntil: "networkidle" });
  const listCount = await page.getByLabel("Premade Deck List").locator("li").count();
  expect(listCount).toBeGreaterThan(0);
});
