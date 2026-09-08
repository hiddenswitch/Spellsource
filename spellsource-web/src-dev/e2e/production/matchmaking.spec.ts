import { expect, test, type APIRequestContext } from "@playwright/test";
import WebSocket from "ws";
import { graphqlHosts } from "./hosts";

type GraphQLResponse = { data?: any; errors?: unknown[] };

async function graphql(request: APIRequestContext, host: string, query: string, variables = {}, token?: string) {
  const response = await request.post(`https://${host}/graphql`, {
    data: { query, variables },
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  expect(response.ok(), `${host} responded ${response.status()}`).toBeTruthy();
  const body = (await response.json()) as GraphQLResponse;
  expect(body.errors, `graphql errors from ${host}`).toBeUndefined();
  return body.data;
}

/** Opens matchFound over graphql-transport-ws and resolves with the first game id. */
function awaitMatchFound(host: string, token: string, timeoutMs: number): { ready: Promise<void>; found: Promise<string>; close: () => void } {
  const ws = new WebSocket(`wss://${host}/subscriptions?accessToken=${token}`, "graphql-transport-ws");
  let resolveReady!: () => void;
  let resolveFound!: (gameId: string) => void;
  let reject!: (e: Error) => void;
  const ready = new Promise<void>((r) => (resolveReady = r));
  const found = new Promise<string>((r, j) => {
    resolveFound = r;
    reject = j;
  });
  const timer = setTimeout(() => reject(new Error(`no matchFound from ${host} within ${timeoutMs}ms`)), timeoutMs);
  ws.on("error", (e) => reject(e));
  ws.on("open", () => ws.send(JSON.stringify({ type: "connection_init", payload: { Authorization: `Bearer ${token}` } })));
  ws.on("message", (raw) => {
    const message = JSON.parse(raw.toString());
    switch (message.type) {
      case "connection_ack":
        ws.send(JSON.stringify({ type: "subscribe", id: "match-found", payload: { query: "subscription { matchFound { gameId } }" } }));
        resolveReady();
        break;
      case "next":
        clearTimeout(timer);
        resolveFound(message.payload.data.matchFound.gameId);
        break;
      case "error":
        clearTimeout(timer);
        reject(new Error(`subscription error from ${host}: ${raw.toString()}`));
        break;
    }
  });
  return { ready, found, close: () => ws.close() };
}

for (const host of graphqlHosts) {
  test(`a guest is matched into a bot game through ${host}`, async ({ request }) => {
    const account = await graphql(
      request,
      host,
      `mutation { createAccount(input: {guest: true, decks: true, email: "smoke@spellsource.com", username: "smoke", password: "smoke"}) {
         accessToken { token } userEntity { id } } }`
    );
    const token: string = account.createAccount.accessToken.token;

    const decks = await graphql(request, host, "{ decks { collection { id name } } }", {}, token);
    expect(decks.decks.length, "guest received no starter decks").toBeGreaterThan(0);
    const deckId: string = decks.decks[0].collection.id;

    const match = awaitMatchFound(host, token, 45_000);
    try {
      await match.ready;
      const enqueue = await graphql(
        request,
        host,
        "mutation Enqueue($input: MatchmakingEnqueueInput!) { enqueueMatchmaking(input: $input) }",
        { input: { deckId, queueId: "quickPlay" } },
        token
      );
      expect(enqueue.enqueueMatchmaking).toBe(true);

      const gameId = await match.found;
      expect(gameId).toBeTruthy();

      const inMatch = await graphql(request, host, "{ isInMatch }", {}, token);
      expect(inMatch.isInMatch).toBe(gameId);
    } finally {
      match.close();
    }
  });
}
