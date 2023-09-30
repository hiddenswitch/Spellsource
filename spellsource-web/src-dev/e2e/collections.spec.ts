import { expect, test } from "../playwright/fixtures";

test("should navigate to collection and see premade decks", async (context) => {
  const { page } = context;
  await page.goto("/home");
  await page.getByLabel("Collection").click();
  const listCount = await page.getByLabel("Premade Deck List").locator("li").count();
  expect(listCount).toBeGreaterThan(0);
});
