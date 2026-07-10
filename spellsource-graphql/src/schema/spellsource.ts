import { buildHTTPExecutor } from "@graphql-tools/executor-http";
import { schemaFromExecutor, wrapSchema } from "@graphql-tools/wrap";
import { Executor, observableToAsyncIterable } from "@graphql-tools/utils";
import { getOperationAST, print } from "graphql";
import { createClient } from "graphql-ws";
import WebSocket from "ws";
import { spellsourceHost, spellsourcePort } from "../config";
import { AuthRequest } from "../auth";

// HTTP executor — used for queries and mutations.
const httpExecutor = buildHTTPExecutor({
  endpoint: `http://${spellsourceHost}:${spellsourcePort}/graphql`,
  fetch: (url, init, context, info) => {
    const req = context as AuthRequest;

    if (req?.token && init) {
      const headers = new Headers(init.headers);
      headers.set("Authorization", `Bearer ${req.token}`);
      init.headers = headers;
    }

    return fetch(url, init);
  },
});

// WebSocket executor — used for subscriptions. Forwards subscription operations
// to the Java backend's graphql-transport-ws endpoint at /graphql, opening a fresh
// graphql-ws connection per subscription so the auth token can be passed via
// connection_init payload (which the backend's GraphQLWSHandler reads).
const wsExecutor: Executor = (executionRequest) => {
  const req = executionRequest.context as AuthRequest | undefined;
  const token = req?.token;

  const client = createClient({
    url: `ws://${spellsourceHost}:${spellsourcePort}/graphql`,
    webSocketImpl: WebSocket,
    connectionParams: token ? { Authorization: `Bearer ${token}` } : {},
    lazy: false,
  });

  return observableToAsyncIterable({
    subscribe: (observer) => {
      const unsubscribe = client.subscribe(
        {
          query: print(executionRequest.document),
          variables: executionRequest.variables as Record<string, unknown>,
          operationName: executionRequest.operationName,
          extensions: executionRequest.extensions as Record<string, unknown>,
        },
        {
          next: (data) => observer.next?.(data as never),
          error: (err) => {
            if (!observer.error) return;
            if (err instanceof Error) {
              observer.error(err);
            } else if (Array.isArray(err)) {
              observer.error(new Error(err.map((e) => (e as { message: string }).message).join(", ")));
            } else {
              observer.error(new Error(`Subscription error: ${JSON.stringify(err)}`));
            }
          },
          complete: () => observer.complete?.(),
        },
      );
      return {
        unsubscribe: () => {
          unsubscribe();
          void client.dispose();
        },
      };
    },
  });
};

const executor: Executor = (executionRequest) => {
  const operationAST = getOperationAST(executionRequest.document, executionRequest.operationName);
  if (operationAST?.operation === "subscription") {
    return wsExecutor(executionRequest);
  }
  return httpExecutor(executionRequest);
};

export const createSpellsourceSchema = async () => {
  while (true) {
    try {
      return wrapSchema({
        schema: await schemaFromExecutor(httpExecutor),
        executor,
      });
    } catch (e) {
      console.log("Spellsource backend not ready yet, retrying in 5s");
      await new Promise((resolve) => setTimeout(resolve, 5e3));
    }
  }
};
