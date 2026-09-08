import { expect, test, type APIRequestContext, type Page } from "@playwright/test";

// The client sends graphql over fetch with a blob body that playwright does not
// expose, so operations are recognised from their response data keys instead.
function watchGraphQL(page: Page) {
  const seen: { key: string; data: any }[] = [];
  let origin = "";
  page.on("response", async (response) => {
    if (response.request().method() !== "POST" || !response.url().includes("/graphql")) return;
    try {
      const body = await response.json();
      origin = new URL(response.url()).origin;
      for (const key of Object.keys(body?.data ?? {})) {
        seen.push({ key, data: body.data[key] });
      }
    } catch {
      // not json
    }
  });
  return {
    seen,
    get origin() {
      return origin;
    },
    async waitFor(key: string, timeout: number, predicate: (data: any) => boolean = () => true) {
      const deadline = Date.now() + timeout;
      while (Date.now() < deadline) {
        const match = seen.filter((s) => s.key === key && predicate(s.data)).at(-1);
        if (match) return match.data;
        await page.waitForTimeout(250);
      }
      const keys = [...new Set(seen.map((s) => s.key))].join(", ") || "nothing";
      throw new Error(`no ${key} response within ${timeout}ms; saw ${keys}`);
    },
    clear() {
      seen.length = 0;
    },
  };
}

async function waitForClient(page: Page) {
  await expect(page.getByRole("progressbar")).toBeHidden({ timeout: 120_000 });
}

/**
 * Unity draws into a canvas with no DOM to wait on, and its screens take input
 * some unknown time after they are drawn. Repeat an interaction until the
 * network response it must produce shows up.
 */
async function until<T>(act: () => Promise<void>, signal: () => Promise<T>, attempts = 12): Promise<T> {
  let last: unknown;
  for (let i = 0; i < attempts; i++) {
    await act();
    try {
      return await signal();
    } catch (e) {
      last = e;
    }
  }
  throw last;
}

async function currentGameId(request: APIRequestContext, origin: string, token: string): Promise<string | null> {
  const response = await request.post(`${origin}/graphql`, {
    data: { query: "{ isInMatch }" },
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(response.ok()).toBeTruthy();
  return (await response.json()).data.isInMatch ?? null;
}

test.describe("browser client session", () => {
  test("a guest's match survives a page refresh", async ({ page, request }) => {
    const graphql = watchGraphQL(page);
    const { width, height } = page.viewportSize()!;
    const click = (fx: number, fy: number) => page.mouse.click(width * fx, height * fy);

    await page.goto("/game");
    await waitForClient(page);

    const created = await until(
      () => click(0.499, 0.798), // Play as Guest
      () => graphql.waitFor("createAccount", 5_000)
    );
    const userId: string = created.userEntity.id;
    const token: string = created.accessToken.token;

    // these screens produce no http traffic to key off, so give each the settle
    // time the client needs rather than retrying a sequence that is not
    // idempotent once the first click has changed screens
    await page.waitForTimeout(15_000); // main menu
    await click(0.787, 0.189); // Single Player
    await page.waitForTimeout(15_000);
    await click(0.142, 0.37); // first starter deck
    await page.waitForTimeout(5_000);
    await click(0.934, 0.898); // PLAY
    // the enqueue itself travels over the client's websocket; the connection to
    // the created game is the first thing visible over http
    await graphql.waitFor("connectToGame", 90_000, (connected) => connected === true);
    await page.waitForTimeout(5_000); // let the mulligan render so the match is genuinely under way
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
