import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./src-dev/e2e/android",
  timeout: 300_000,
  workers: 1,
  retries: 0,
  reporter: [["list"], ["html", { open: "never", outputFolder: "playwright-report/android" }]],
  outputDir: "test-results/android-webgl",
});
