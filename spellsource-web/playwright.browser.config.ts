import { defineConfig, devices } from "@playwright/test";

const publicUrl = process.env.SPELLSOURCE_BASE_URL || undefined;

export default defineConfig({
  testDir: "./src-dev/e2e/production",
  testMatch: ["browser-mobile.spec.ts", "resume.spec.ts"],
  timeout: 180_000,
  retries: 0,
  workers: 1,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: publicUrl ?? "http://127.0.0.1:3200",
    screenshot: "only-on-failure",
    trace: "retain-on-failure",
  },
  projects: [
    { name: "desktop", use: { browserName: "chromium", viewport: { width: 1440, height: 810 },
      launchOptions: { args: ["--use-gl=angle", "--use-angle=swiftshader", "--enable-unsafe-swiftshader"] } } },
    ...["Pixel 7", "Pixel 7 landscape", "iPhone 13", "iPhone 13 landscape"].map(name => ({
      name, testMatch: "browser-mobile.spec.ts", use: { ...devices[name],
        launchOptions: name.startsWith("Pixel") ? { args: ["--use-gl=angle", "--use-angle=swiftshader", "--enable-unsafe-swiftshader"] } : {} },
    })),
  ],
  webServer: publicUrl ? undefined : {
    command: "yarn dev -H 127.0.0.1 -p 3200",
    url: "http://127.0.0.1:3200/game",
    timeout: 120_000,
    reuseExistingServer: !process.env.CI,
  },
});
