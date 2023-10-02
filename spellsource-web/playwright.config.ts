import { defineConfig } from "@playwright/test";

export const baseURL = "http://127.0.0.1:3000";
export default defineConfig({
  webServer: [
    {
      command: "gradle :spellsource-server:run",
      url: "http://127.0.0.1:8080/readiness",
      reuseExistingServer: !process.env.CI,
      stdout: "ignore",
      stderr: "pipe",
      timeout: 120 * 1000,
    },
    {
      command: "yarn run dev",
      url: "http://127.0.0.1:3000/api/readiness",
      reuseExistingServer: !process.env.CI,
      stdout: "ignore",
      stderr: "pipe",
      timeout: 120 * 1000,
    },
    {
      command: "gradle :spellsource-art-generation:run",
      url: "http://127.0.0.1:8188",
      reuseExistingServer: !process.env.CI,
      stdout: "ignore",
      stderr: "pipe",
      timeout: 120 * 1000,
    },
  ],
  use: {
    baseURL: baseURL,
  },
});
