import { expect, test } from "@playwright/test";
import { promises as dns } from "node:dns";
import { graphqlHosts } from "./hosts";
import { acceptedClients, addToxic, clearToxics, dockerAvailable, hostResolverRules, setEnabled, startToxiproxy, stopToxiproxy } from "./toxiproxy";
import { currentGameId, driveGuestIntoMatch, waitForClient, watchConsole, watchGraphQL, watchSockets } from "./client";

/**
 * Network resilience of the browser client during a match, measured through a
 * toxiproxy in front of the gateway. Three ways the network goes away:
 *
 *   reset     — the connection is torn down with a TCP reset (server crash, NAT
 *               table flush): the browser learns immediately.
 *   closed    — the proxy is disabled: connections close cleanly and new ones
 *               are refused (gateway restart).
 *   blackhole — packets vanish in both directions with no close (wifi out of
 *               range, laptop lid, cellular dead zone): nothing tells the
 *               browser unless the client notices missing traffic.
 *
 * For each, the client must show its reconnecting screen promptly and, once the
 * path is back, reconnect to the same game without user action. The
 * reconnecting screen is driven by SpellsourceClient.isConnectionHealthy, which
 * flips exactly when the game subscription errors and logs
 * "Game subscription error", so that log line is the observable; resumption is
 * a fresh websocket carrying game frames plus "ConnectToGame result: True".
 */

const REQUIRED_DETECT_MS = 10_000;
const REQUIRED_RESUME_MS = 20_000;

test.use({
  launchOptions: {
    args: ["--use-gl=angle", "--use-angle=swiftshader", "--enable-unsafe-swiftshader", hostResolverRules(graphqlHosts)],
  },
});

test.describe("gateway outage during a match", () => {
  test.skip(!dockerAvailable(), "needs docker for toxiproxy");

  test.beforeAll(async () => {
    const [address] = await dns.resolve4(graphqlHosts[0]);
    await startToxiproxy(address);
  });

  test.afterAll(() => stopToxiproxy());

  test.afterEach(() => clearToxics());

  for (const scenario of ["reset", "closed", "blackhole", "offline", "lookup-failure"] as const) {
    test(`${scenario}: reconnecting screen appears promptly and the match resumes`, async ({ page, request }, testInfo) => {
      test.setTimeout(6 * 60_000);
      const graphql = await watchGraphQL(page);
      const console_ = watchConsole(page);
      const sockets = watchSockets(page);

      await page.goto("/game");
      await waitForClient(page);
      const { token } = await driveGuestIntoMatch(page, graphql);
      const gameId = await currentGameId(request, graphql.origin, token);
      expect(gameId, "the client should be in a game before the outage").toBeTruthy();
      const before = await page.screenshot();
      await testInfo.attach(`${scenario}-before-outage`, { body: before, contentType: "image/png" });
      const liveBefore = sockets.live.length;
      expect(liveBefore, "the client should hold a live websocket to the gateway").toBeGreaterThan(0);
      expect(acceptedClients(), "the browser's gateway traffic must be flowing through toxiproxy").toBeGreaterThan(0);

      // ── sever ──
      const cutAt = Date.now();
      if (scenario === "reset") {
        await addToxic({ name: "rst", type: "reset_peer", stream: "downstream", attributes: { timeout: 0 } });
      } else if (scenario === "closed" || scenario === "lookup-failure") {
        await setEnabled(false);
      } else if (scenario === "offline") {
        // the browser itself reports the network gone (wifi off, airplane mode);
        // chromium keeps established sockets, so this is also a blackhole
        await addToxic({ name: "hole-down", type: "timeout", stream: "downstream", attributes: { timeout: 0 } });
        await addToxic({ name: "hole-up", type: "timeout", stream: "upstream", attributes: { timeout: 0 } });
        await page.context().setOffline(true);
      } else {
        await addToxic({ name: "hole-down", type: "timeout", stream: "downstream", attributes: { timeout: 0 } });
        await addToxic({ name: "hole-up", type: "timeout", stream: "upstream", attributes: { timeout: 0 } });
      }

      // A reset is only delivered with the next byte on the wire (toxiproxy's
      // reset_peer acts on traffic), which arrives with the gateway's next
      // keepalive. The browser learns the socket is gone at that moment, so
      // detection is measured from the close event when there is one; for the
      // blackhole there never is one and the clock runs from the cut.
      let detectedMs: number | null = null;
      let closeDelayMs: number | null = null;
      try {
        const line = await console_.waitFor(/Game subscription error/, REQUIRED_DETECT_MS + 15_000, cutAt);
        const closedAt = sockets.sockets.map((s) => s.closedAt ?? 0).filter((t) => t >= cutAt).sort()[0];
        closeDelayMs = closedAt ? closedAt - cutAt : null;
        detectedMs = line.at - Math.max(cutAt, closedAt ?? 0);
      } catch {
        // measured below
      }
      const duringOutage = await page.screenshot();
      await testInfo.attach(`${scenario}-during-outage`, { body: duringOutage, contentType: "image/png" });

      // ── restore ──
      // Reproduce the race where the gateway is reachable again but the first
      // match lookup fails. A successful account query must not end the game.
      if (scenario === "lookup-failure") {
        await page.evaluate(() => {
          const originalFetch = window.fetch;
          (window as any).__failedMatchLookups = 0;
          window.fetch = async function (input, init) {
            const url = input instanceof Request ? input.url : String(input);
            if (url.includes("/graphql")) {
              const body = init?.body != null
                ? await new Response(init.body).text()
                : input instanceof Request ? await input.clone().text() : "";
              if (/isInMatch/i.test(body)) {
                window.fetch = originalFetch;
                (window as any).__failedMatchLookups++;
                return new Response(JSON.stringify({ errors: [{ message: "Injected transient match lookup failure" }] }), {
                  status: 503,
                  headers: { "Content-Type": "application/json" },
                });
              }
            }
            return originalFetch.call(this, input, init);
          };
        });
      }
      const restoredAt = Date.now();
      await clearToxics();
      if (scenario === "offline") {
        await page.context().setOffline(false);
      }

      let resumedMs: number | null = null;
      try {
        const fresh = await sockets.waitForNewSocket(restoredAt, REQUIRED_RESUME_MS);
        await console_.waitFor(/ConnectToGame result: True/i, REQUIRED_RESUME_MS - (Date.now() - restoredAt), restoredAt);
        const deadline = restoredAt + REQUIRED_RESUME_MS;
        while (Date.now() < deadline && fresh.framesReceived === 0) await page.waitForTimeout(100);
        if (fresh.framesReceived > 0) resumedMs = (fresh.lastFrameAt ?? Date.now()) - restoredAt;
      } catch {
        // measured below
      }
      const afterRestore = await page.screenshot();
      await testInfo.attach(`${scenario}-after-restore`, { body: afterRestore, contentType: "image/png" });

      const stillSameGame = await currentGameId(request, graphql.origin, token);
      const timings = { scenario, closeDelayMs, detectedMs, resumedMs, sameGame: stillSameGame === gameId, gameId };
      await testInfo.attach(`${scenario}-timings`, { body: JSON.stringify(timings, null, 2), contentType: "application/json" });
      const evidence = {
        cutAt,
        restoredAt,
        sockets: sockets.sockets.map((s) => ({ url: s.url, openedAt: s.openedAt, closedAt: s.closedAt ?? null, framesReceived: s.framesReceived, lastFrameAt: s.lastFrameAt ?? null })),
        console: console_.lines.filter((l) => l.at >= cutAt - 5_000 && l.type !== "warning").map((l) => `${l.at - cutAt}ms [${l.type}] ${l.text.slice(0, 200)}`),
      };
      await testInfo.attach(`${scenario}-evidence`, { body: JSON.stringify(evidence, null, 2), contentType: "application/json" });
      console.log(`[${scenario}] ${JSON.stringify(timings)}`);
      console.log(`[${scenario}] sockets ${JSON.stringify(evidence.sockets)}`);
      console.log(`[${scenario}] console ${JSON.stringify(evidence.console.slice(0, 12))}`);

      if (scenario === "lookup-failure") {
        expect(await page.evaluate(() => (window as any).__failedMatchLookups)).toBe(1);
        expect(console_.find(/server reports no game/, restoredAt), "a failed lookup must not abandon the active match").toBeUndefined();
      }
      expect(detectedMs, `reconnecting screen not shown within ${REQUIRED_DETECT_MS}ms of the ${scenario}`).not.toBeNull();
      expect(detectedMs!, `reconnecting screen took too long after the ${scenario}`).toBeLessThanOrEqual(REQUIRED_DETECT_MS);
      expect(resumedMs, `match not resumed within ${REQUIRED_RESUME_MS}ms of the network returning`).not.toBeNull();
      expect(stillSameGame, "the server should still report the same game").toBe(gameId);
    });
  }
});
