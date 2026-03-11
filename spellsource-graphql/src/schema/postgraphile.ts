import { wrapSchema } from "@graphql-tools/wrap";
import { grafast } from "grafast";
import { print } from "graphql";
import { makeSchema } from "postgraphile";
import { ExecutionResult } from "@graphql-tools/utils";
import preset from "../graphile.config";

export const createPostgraphileSchema = async () => {
  try {
    const { schema, resolvedPreset } = await makeSchema(preset);

    return wrapSchema({
      schema,
      executor: async (request) =>
        grafast({
          schema,
          resolvedPreset,
          operationName: request.operationName,
          variableValues: request.variables,
          requestContext: request.context,
          source: print(request.document)
        }) as ExecutionResult
    });
  } catch (e) {
    console.error("Failed to create postgraphile schema");
    console.error(e);
    return undefined!;
  }
};
