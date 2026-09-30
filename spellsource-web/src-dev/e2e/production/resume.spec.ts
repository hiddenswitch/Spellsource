import { expect, test } from "@playwright/test";
import { currentGameId, driveGuestIntoMatch, waitForClient, watchGraphQL } from "./client";

test.describe("browser client session", () => {
  test("a guest's match survives a page refresh", async ({ page, request }) => {
    const graphql = await watchGraphQL(page);

    await page.goto("/game");
    await waitForClient(page);
    const { userId, token } = await driveGuestIntoMatch(page, graphql);
    const gameId = await currentGameId(request, graphql.origin, token);
    expect(gameId, "the server should report the match the client just joined").toBeTruthy();

    graphql.clear();
    await page.reload();
    await waitForClient(page);
    // the resumed session is not driven by clicks; the client acts on its own

    // the saved guest must be restored rather than recreated, and the client
    // must find the same game and reconnect to it on its own
    const account = await graphql.waitFor("account", 60_000);
    expect(account.id).toBe(userId);
    const resumedGameId = await graphql.waitFor("isInMatch", 60_000, (id) => typeof id === "string" && id.length > 0);
    expect(resumedGameId).toBe(gameId);
    await graphql.waitFor("connectToGame", 60_000, (connected) => connected === true);
    expect(graphql.seen.some((s) => s.key === "createAccount"), "a new guest was created after refresh").toBe(false);
  });
});
