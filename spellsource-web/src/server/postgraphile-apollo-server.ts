import { ApolloServerPlugin } from "@apollo/server";
import { GraphQLRequestContextDidResolveOperation } from "@apollo/server/src/externalTypes";
import { makeSchema } from "postgraphile";
import { hookArgs } from "grafast";
import { GraphQLRequestListener } from "@apollo/server/src/externalTypes/plugins";

const authorizationBearerRex = /^\s*bearer\s+([a-z0-9\-._~+/]+=*)\s*$/i;

const createBadAuthorizationHeaderError = () =>
  httpError(400, "Authorization header is not of the correct bearer scheme format.");

const getJwtToken = (authorization: string | string[] | null) => {
  if (Array.isArray(authorization)) throw createBadAuthorizationHeaderError();

  // If there was no authorization header, just return null.
  if (authorization == null) return null;

  const match = authorizationBearerRex.exec(authorization);

  // If we did not match the authorization header with our expected format,
  // throw a 400 error.
  if (!match) throw createBadAuthorizationHeaderError();

  // Return the token from our match.
  return match[1];
};

const httpError = (status: unknown, message: string) => {
  const err = new Error(message);
  // @ts-ignore
  err["status"] = status;
  return err;
};

/**
 * This is a version of https://github.com/graphile/postgraphile-apollo-server/blob/master/index.js but in TypeScript
 */
export const makeSchemaAndPlugin = async (preset: GraphileConfig.Preset): Promise<GraphQLRequestListener> => {
  // See https://www.graphile.org/postgraphile/usage-schema/ for schema-only usage guidance
  const {} = preset;

  const makePostgraphileApolloRequestHooks = (resolvedPreset: GraphileConfig.ResolvedPreset) => async () => {
    let finished: ((value?: PromiseLike<any> | any) => void) | null = null;
    return {
      /*
       * Since `requestDidStart` itself is synchronous, we must hijack an
       * asynchronous callback in order to set up our context.
       */
      async didResolveOperation(requestContext: GraphQLRequestContextDidResolveOperation<Record<string, any>>) {
        const { contextValue: context, request: graphqlRequest } = requestContext;

        const { http: req } = graphqlRequest;
        const jwtToken = jwtSecret ? getJwtToken(req) : null;

        try {
          const args = await hookArgs(requestContext, resolvedPreset, requestContext);

          Object.assign(context, args);

          console.log(args);
        } catch (e) {
          console.warn(e);
        }
      },
      async willSendResponse() {
        // Release the context;
        if (finished) {
          finished();
          finished = null;
        }
      },
    };
  };

  try {
    const schema = await makeSchema(preset);

    const plugin = {
      requestDidStart: makePostgraphileApolloRequestHooks(schema.resolvedPreset),
    } as ApolloServerPlugin;
    return {
      schema,
      plugin,
    };
  } catch (e) {
    console.error("Failed to create postgraphile schema");
    console.error(e);
    return {
      schema: undefined,
      plugin: undefined,
    };
  }
};
