import { expect, test } from "@playwright/test";

test.describe("site", () => {
  test("home page leads to the browser client", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("link", { name: "Play Now" })).toHaveAttribute("href", "/game");
    await expect(page.getByRole("link", { name: "Play", exact: true }).first()).toHaveAttribute("href", "/game");
    await expect(page.getByRole("link", { name: "Get the App" })).toHaveAttribute("href", "/download");
  });

  test("download page offers the browser client first", async ({ page }) => {
    await page.goto("/download");
    await expect(page.getByRole("link", { name: /Play Spellsource in your browser/ })).toHaveAttribute("href", "/game");
  });
});
