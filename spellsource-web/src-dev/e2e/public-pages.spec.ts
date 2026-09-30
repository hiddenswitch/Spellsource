import { expect, test } from "../playwright/fixtures";

test("public pages expose the shared navigation", async ({ page }) => {
  await page.goto("/download");
  await expect(page.getByRole("banner")).toContainText("Spellsource");
  await expect(page.getByRole("banner").getByText("Collection")).toBeVisible();
  await expect(page.getByRole("heading", { name: "Choose your next adventure." })).toBeVisible();
});

test("collection defaults to cards and supports the dense table", async ({ page }) => {
  await page.goto("/collection");
  await expect(page.getByRole("button", { name: "Cards" })).toBeVisible();
  await page.getByRole("button", { name: "Table" }).click();
  await expect(page.locator("table")).toBeVisible();
  await expect(page).toHaveURL(/view=table/);
});

test("mobile navigation opens without horizontal overflow", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/wiki");
  await page.locator("header svg").click();
  await expect(page.getByRole("banner").getByText("Docs")).toBeVisible();
  await expect(page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).resolves.toBeTruthy();
});
