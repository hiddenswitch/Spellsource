import { Pool } from "pg";
import { PostGraphileOptions, createPostGraphileSchema, withPostGraphileContext } from "postgraphile";
import {ApolloServerPlugin, BaseContext} from "@apollo/server";
import {GraphQLRequestContextDidResolveOperation} from "@apollo/server/src/externalTypes";

const authorizationBearerRex = /^\s*bearer\s+([a-z0-9\-._~+/]+=*)\s*$/i;

const createBadAuthorizationHeaderError = () =>
  httpError(400, "Authorization header is not of the correct bearer scheme format.");

const getJwtToken = (request) => {
  const authorization = request.headers.get("authorization");
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

const httpError = (status, message) => {
  const err = new Error(message);
  // @ts-ignore
  err.status = status;
  return err;
};

/**
 * This is a version of https://github.com/graphile/postgraphile-apollo-server/blob/master/index.js but in TypeScript
 */
export const makeSchemaAndPlugin = async (
  pgPool: Pool,
  dbSchema: string | string[],
  postGraphileOptions: PostGraphileOptions
) => {
  if (!pgPool || typeof pgPool !== "object") {
    throw new Error("The first argument must be a pgPool instance");
  }

  // See https://www.graphile.org/postgraphile/usage-schema/ for schema-only usage guidance
  const { pgSettings: pgSettingsGenerator, additionalGraphQLContextFromRequest, jwtSecret } = postGraphileOptions;

  async function makePostgraphileApolloRequestHooks(): Promise<any> {
    let finished;
    return {
      /*
       * Since `requestDidStart` itself is synchronous, we must hijack an
       * asynchronous callback in order to set up our context.
       */
      async didResolveOperation(requestContext: GraphQLRequestContextDidResolveOperation<Record<string, any>>) {
        const { contextValue: context, request: graphqlRequest } = requestContext;

        /*
         * Get access to the original HTTP request to determine the JWT and
         * also perform anything needed for pgSettings support.  (Actually this
         * is a subset of the original HTTP request according to the Apollo
         * Server typings, it only contains "headers"?)
         */
        const { http } = graphqlRequest;
        const req = {...http, context}

        /*
         * The below code implements similar logic to this area of
         * PostGraphile:
         *
         * https://github.com/graphile/postgraphile/blob/ff620cac86f56b1cd58d6a260e51237c19df3017/src/postgraphile/http/createPostGraphileHttpRequestHandler.ts#L114-L131
         */

        // Extract the JWT if present:
        const jwtToken = jwtSecret ? getJwtToken(req) : null;

        // Extract additional context
        const additionalContext =
          typeof additionalGraphQLContextFromRequest === "function"
            ? await additionalGraphQLContextFromRequest(req as any, undefined as any)
            : {};

        // Perform the `pgSettings` callback, if appropriate
        const pgSettings =
          typeof pgSettingsGenerator === "function" ? await pgSettingsGenerator(req as any) : pgSettingsGenerator;

        // Finally add our required properties to the context
        const withContextOptions = {
          ...postGraphileOptions,
          pgSettings,
          pgPool,
          jwtToken: jwtToken ?? "",
        };

        await new Promise<void>((resolve, reject) => {
          withPostGraphileContext(
            withContextOptions,
            (postgraphileContext) =>
              new Promise((releaseContext) => {
                // Jesse, an Apollo Server developer, told me to do this 😜
                Object.assign(context, additionalGraphQLContextFromRequest, postgraphileContext);

                /*
                 * Don't resolve (don't release the pgClient on context) until
                 * the request is complete.
                 */
                finished = releaseContext;

                // The context is now ready to be used.
                resolve();
              })
          ).catch((e) => {
            // console.error("Error occurred creating context!");
            console.error(e);
            // Release context
            if (finished) {
              finished();
              finished = null;
            }

            reject(e);
          });
        });
      },
      async willSendResponse(context) {
        // Release the context;
        if (finished) {
          finished();
          finished = null;
        }
      },
    };
  }

  try {
    const schema = await createPostGraphileSchema(pgPool, dbSchema, postGraphileOptions);

    const plugin = {
      requestDidStart: makePostgraphileApolloRequestHooks,
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
