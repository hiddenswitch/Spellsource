import { test as baseTest } from "@playwright/test";
import { v4 as uuidv4 } from "uuid";
import fs from "fs";
import path from "path";

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

      const page = await browser.newPage({ storageState: undefined });

      // Step 1: Visit "/"
      await page.goto("/");
      await page.getByRole("link", { name: "Login" }).click();
      await page.getByRole("link", { name: "Register" }).click();

      // Step 4: Populate the Email field
      const randomEmail = `user+${uuidv4()}@spellsource.com`;
      await page.getByLabel("Email").fill(randomEmail);

      // Step 5: Populate the Username field
      const randomUsername = uuidv4();
      await page.getByLabel("Username").fill(randomUsername);

      // Step 6: Populate the Password and "Confirm password" fields
      const randomPassword = uuidv4();
      await page.getByLabel("Password").fill(randomPassword);
      await page.getByLabel("Confirm Password").fill(randomPassword);

      // Step 7: Choose "With Premade Decks" in the "Starting Collection" dropdown
      await page.getByLabel("Starting Collection").selectOption("With Premade Decks");

      // Step 8: Click the Register input/submit
      await page.getByRole("button", { name: "Register" }).click();

      // Wait for registration to complete and possibly for redirection to a certain page
      await page.waitForURL("/home");

      await page.context().storageState({ path: fileName });
      await page.close();
      await use(fileName);
    },
    { scope: "worker" },
  ],
});
