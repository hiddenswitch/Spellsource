import { expect, type APIRequestContext, type Page, type WebSocket } from "@playwright/test";

/**
 * Helpers for driving the Unity WebGL client through playwright. The client
 * draws into a canvas, so it is driven by coordinates measured at the
 * 1440x810 viewport the production config sets, and observed through the
 * traffic and console output it produces.
 */

// The client sends graphql over fetch with a blob body that playwright does not
// expose, so operations are recognised from their response data keys instead.
export async function watchGraphQL(page: Page) {
  const seen: { key: string; data: any; at: number }[] = [];
  let origin = "";
  // Observe a clone inside the browser: Chromium can evict network response
  // bodies while loading the large Unity wasm/data files. This preserves the
  // browser's network path, including toxiproxy in the outage tests.
  await page.exposeBinding("__spellsourceObserveGraphQL", (_source, url: string, body: any) => {
    origin = new URL(url).origin;
    for (const key of Object.keys(body?.data ?? {})) {
      seen.push({ key, data: body.data[key], at: Date.now() });
    }
  });
  await page.addInitScript(() => {
    const originalFetch = window.fetch;
    window.fetch = async function (...args) {
      const response = await originalFetch.apply(this, args);
      if (response.url.includes("/graphql")) {
        void response.clone().json().then(body =>
          (window as any).__spellsourceObserveGraphQL(response.url, body)
        ).catch(() => {});
      }
      return response;
    };
  });
  return {
    seen,
    get origin() {
      return origin;
    },
    async waitFor(key: string, timeout: number, predicate: (data: any) => boolean = () => true, since = 0) {
      const deadline = Date.now() + timeout;
      while (Date.now() < deadline) {
        const match = seen.filter((s) => s.at >= since && s.key === key && predicate(s.data)).at(-1);
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

/** Console output from the Unity client, timestamped, for waiting on log lines. */
export function watchConsole(page: Page) {
  const lines: { type: string; text: string; at: number }[] = [];
  page.on("console", (m) => lines.push({ type: m.type(), text: m.text(), at: Date.now() }));
  return {
    lines,
    async waitFor(pattern: RegExp, timeout: number, since = 0) {
      const deadline = Date.now() + timeout;
      while (Date.now() < deadline) {
        const match = lines.find((l) => l.at >= since && pattern.test(l.text));
        if (match) return match;
        await page.waitForTimeout(100);
      }
      throw new Error(`no console line matching ${pattern} within ${timeout}ms`);
    },
    find(pattern: RegExp, since = 0) {
      return lines.find((l) => l.at >= since && pattern.test(l.text));
    },
  };
}

/** Every WebSocket the page opens, with open/close times and frame counts. */
export function watchSockets(page: Page) {
  const sockets: { ws: WebSocket; url: string; openedAt: number; closedAt?: number; framesReceived: number; lastFrameAt?: number }[] = [];
  page.on("websocket", (ws) => {
    if (new URL(ws.url()).pathname !== "/subscriptions") return;
    const entry = { ws, url: new URL(ws.url()).origin + new URL(ws.url()).pathname, openedAt: Date.now(), framesReceived: 0 } as (typeof sockets)[number];
    sockets.push(entry);
    ws.on("framereceived", () => {
      entry.framesReceived++;
      entry.lastFrameAt = Date.now();
    });
    ws.on("close", () => (entry.closedAt = Date.now()));
  });
  return {
    sockets,
    get live() {
      return sockets.filter((s) => !s.closedAt);
    },
    async waitForNewSocket(since: number, timeout: number) {
      const deadline = Date.now() + timeout;
      while (Date.now() < deadline) {
        const s = sockets.find((x) => x.openedAt >= since);
        if (s) return s;
        await page.waitForTimeout(100);
      }
      throw new Error(`no new websocket within ${timeout}ms`);
    },
  };
}

export async function waitForClient(page: Page) {
  // A hidden/absent loading indicator can be observed before hydration. The
  // session event is emitted by Unity itself after the runtime starts.
  await expect(page.locator("html")).toHaveAttribute("data-game-session", /.+/, { timeout: 120_000 });
  await expect(page.getByRole("progressbar")).toBeHidden({ timeout: 120_000 });
}

/**
 * Unity draws into a canvas with no DOM to wait on, and its screens take input
 * some unknown time after they are drawn. Repeat an interaction until the
 * network response it must produce shows up.
 */
export async function until<T>(act: () => Promise<void>, signal: () => Promise<T>, attempts = 12): Promise<T> {
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

export async function currentGameId(request: APIRequestContext, origin: string, token: string): Promise<string | null> {
  const response = await request.post(`${origin}/graphql`, {
    data: { query: "{ isInMatch }" },
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(response.ok()).toBeTruthy();
  return (await response.json()).data.isInMatch ?? null;
}

/**
 * Play as Guest, then Single Player with the first starter deck, and return
 * once the client has connected to the created game.
 */
export async function driveGuestIntoMatch(page: Page, graphql: Awaited<ReturnType<typeof watchGraphQL>>, touch = false) {
  const click = async (fx: number, fy: number) => {
    const bounds = await page.locator("canvas").boundingBox();
    if (!bounds) throw new Error("The game canvas is not visible");
    const x = bounds.x + bounds.width * fx;
    const y = bounds.y + bounds.height * fy;
    if (touch) await page.touchscreen.tap(x, y);
    else await page.mouse.click(x, y);
  };

  const created = await until(
    async () => {
      // The form is centered with a fixed pixel-art layout in Unity render pixels.
      // Account for the browser DPR cap instead of assuming a desktop viewport.
      const geometry = await page.locator("canvas").evaluate(canvas => ({
        width: (canvas as HTMLCanvasElement).width, height: (canvas as HTMLCanvasElement).height,
      }));
      const uiScale = Math.min(1, geometry.width / 800, geometry.height / 800);
      await click(0.499, 0.5 + 240 * uiScale / geometry.height);
    },
    () => graphql.waitFor("createAccount", 5_000)
  );
  const userId: string = created.userEntity.id;
  const token: string = created.accessToken.token;

  // Canvas screens can be visible before accepting input. Retry the menu/deck
  // selection until the real game connection confirms that setup completed.
  await until(async () => {
    const geometry = await page.locator("canvas").evaluate(canvas => ({
      width: (canvas as HTMLCanvasElement).width, height: (canvas as HTMLCanvasElement).height,
      portrait: canvas.clientHeight > canvas.clientWidth,
    }));
    const uiScale = Math.min(1, geometry.width / 800, geometry.height / 800);
    await click(geometry.portrait ? 0.5 : 0.787,
      geometry.portrait ? 1 - 680 * uiScale / geometry.height : 0.189); // Single Player
    await page.waitForTimeout(1_000);
    await click(205 * uiScale / geometry.width, geometry.portrait ? 0.25 : 0.37); // first starter deck
    await page.waitForTimeout(1_000);
    await click(1 - 90 * uiScale / geometry.width, 1 - 82 * uiScale / geometry.height); // PLAY
  }, () => graphql.waitFor("connectToGame", 5_000, connected => connected === true), 18);
  await page.waitForTimeout(5_000); // let the mulligan render so the match is genuinely under way
  return { userId, token };
}
