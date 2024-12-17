import { makeSchema } from "postgraphile";
import { wrapSchema } from "@graphql-tools/wrap";
import { grafast } from "grafast";
import { ExecutionResult } from "@graphql-tools/utils";
// @ts-ignore
import { print } from "postgraphile/graphql";

export const createPostgraphileSchema = async (preset: GraphileConfig.Preset) => {
  try {
    if (process.env.VERBOSE == "true") {
      console.log("Starting to create postgraphile schema");
    }
    const { schema, resolvedPreset } = await makeSchema(preset);
    if (process.env.VERBOSE == "true") {
      console.log("Successfully made initial schema");
    }

    const postgraphileSchema = wrapSchema({
      schema,
      executor: async (request) =>
        grafast({
          schema,
          resolvedPreset,
          operationName: request.operationName,
          variableValues: request.variables,
          requestContext: request.context,
          source: print(request.document),
        }) as ExecutionResult,
    });

    if (process.env.VERBOSE == "true") {
      console.log("Successfully created postgraphile schema");
    }

    return postgraphileSchema;
  } catch (e) {
    console.error("Failed to create postgraphile schema");
    console.error(e);
    return undefined!;
  }
};
