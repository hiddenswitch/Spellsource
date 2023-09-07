import type { JestConfigWithTsJest } from "ts-jest";

export default {
  preset: "ts-jest",
  testEnvironment: "jsdom",
} as JestConfigWithTsJest;
