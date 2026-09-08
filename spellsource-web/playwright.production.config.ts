import { defineConfig } from "@playwright/test";

/**
 * Smoke tests against the deployed site. No servers are started: everything
 * runs against SPELLSOURCE_SITE_URL (default https://playspellsource.com) and
 * the public GraphQL hosts, exercising DNS, TLS, the in-page WebGL client and
 * matchmaking through the gateway the way the shipped client does.
 *
 *   yarn test:production
 */
export const siteURL = process.env.SPELLSOURCE_SITE_URL ?? "https://playspellsource.com";

export default defineConfig({
  testDir: "./src-dev/e2e/production",
  timeout: 180 * 1000,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? [["github"], ["html", { open: "never" }]] : "list",
  use: {
    baseURL: siteURL,
    trace: "retain-on-failure",
    launchOptions: {
      // the unity client needs a webgl2 context; software rendering is enough
      args: ["--use-gl=angle", "--use-angle=swiftshader", "--enable-unsafe-swiftshader"],
    },
  },
});
