import { buildHTTPExecutor } from "@graphql-tools/executor-http";
import { schemaFromExecutor, wrapSchema } from "@graphql-tools/wrap";
import { spellsourceHost, spellsourcePort } from "../config";
import { AuthRequest } from "../auth";

const executor = buildHTTPExecutor({
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

export const createSpellsourceSchema = async () =>
  wrapSchema({
    schema: await schemaFromExecutor(executor),
    executor,
  });
