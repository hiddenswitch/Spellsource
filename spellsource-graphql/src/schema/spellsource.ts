import { buildHTTPExecutor } from "@graphql-tools/executor-http";
import { buildGraphQLWSExecutor } from "@graphql-tools/executor-graphql-ws";
import { schemaFromExecutor, wrapSchema } from "@graphql-tools/wrap";
import type { ExecutionRequest, Executor } from "@graphql-tools/utils";
import { getOperationAST } from "graphql";
import { spellsourceHost, spellsourcePort } from "../config";
import { AuthRequest } from "../auth";

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

/**
 * Combined executor: HTTP for queries/mutations, WS for subscriptions.
 * Creates a fresh WS connection per subscription, forwarding auth from
 * the gateway's connection context.
 */
const combinedExecutor: Executor = (request: ExecutionRequest) => {
  const operation = getOperationAST(request.document, request.operationName ?? undefined);

  if (operation?.operation === "subscription") {
    const ctx = request.context as { connectionParams?: Record<string, string> } | undefined;
    const token = ctx?.connectionParams?.Authorization ?? "";

    const wsExecutor = buildGraphQLWSExecutor({
      url: `ws://${spellsourceHost}:${spellsourcePort}/graphql`,
      connectionParams: { Authorization: token },
      lazy: true,
    });

    return wsExecutor(request);
  }

  return httpExecutor(request);
};

export const createSpellsourceSchema = async () =>
  wrapSchema({
    schema: await schemaFromExecutor(httpExecutor),
    executor: combinedExecutor,
  });
