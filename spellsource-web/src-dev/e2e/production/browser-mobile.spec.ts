import { expect, test } from "@playwright/test";

test("the exact candidate starts at the device viewport", async ({ page }) => {
  const errors: string[] = [];
  page.on("pageerror", error => errors.push(error.message));
  await page.goto("/game");
  await expect(page.getByRole("progressbar")).toBeHidden({ timeout: 120_000 });
  await expect(page.getByText("Spellsource could not continue.")).toBeHidden();
  await expect(page.locator("canvas")).toBeVisible();
  if (process.env.SPELLSOURCE_EXPECTED_BUILD_ID) {
    await expect(page.locator("html")).toHaveAttribute("data-webgl-build", process.env.SPELLSOURCE_EXPECTED_BUILD_ID);
  }
  const canvas = await page.locator("canvas").evaluate(c => ({ width: c.width, css: c.clientWidth }));
  expect(canvas.width).toBeLessThanOrEqual(canvas.css * 2 + 1);
  expect(errors).toEqual([]);
});
