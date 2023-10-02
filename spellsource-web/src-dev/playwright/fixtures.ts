import { test as baseTest } from "@playwright/test";
import { v4 as uuidv4 } from "uuid";
import fs from "fs";
import path from "path";
import { baseURL } from "../../playwright.config";

export * from "@playwright/test";
export const test = baseTest.extend<{}, { workerStorageState: string }>({
  storageState: ({ workerStorageState }, use) => use(workerStorageState),

  workerStorageState: [
    async ({ browser }, use) => {
      const id = test.info().parallelIndex;
      const fileName = path.resolve(test.info().project.outputDir, `.auth/${id}.json`);

      if (fs.existsSync(fileName)) {
        await use(fileName);
        return;
      }
      const context = await browser.newContext({ storageState: undefined, baseURL: baseURL });
      const page = await context.newPage();

      await page.goto(`/`);
      await page.getByLabel("Login").click();
      await page.waitForURL(/realms\/hiddenswitch\//, { waitUntil: "domcontentloaded" });
      await page.getByText("Register").click();
      await page.waitForURL(/realms\/hiddenswitch\/login-actions\//, { waitUntil: "domcontentloaded" });

      const randomEmail = `user+${uuidv4()}@spellsource.com`;
      await page.getByLabel("Email").fill(randomEmail);

      const randomUsername = uuidv4();
      await page.getByLabel("Username").fill(randomUsername);

      const randomPassword = uuidv4();
      await page.getByLabel("Password", { exact: true }).fill(randomPassword);
      await page.getByLabel("Confirm Password").fill(randomPassword);

      await page.getByLabel("Starting Collection").selectOption("With Premade Decks");

      await page.getByRole("button", { name: "Register" }).click();

      await page.waitForURL(`${baseURL}/home`);

      await context.storageState({ path: fileName });
      await page.close();
      await context.close();
      await use(fileName);
    },
    { scope: "worker" },
  ],
});
