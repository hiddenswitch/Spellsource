import { ApolloServer } from "@apollo/server";
import { expressMiddleware } from "@apollo/server/express4";
import { ApolloServerPluginLandingPageDisabled } from "@apollo/server/plugin/disabled";
import express, { Application, NextFunction, Request, Response } from "express";
import { createFullSchema } from "./schema/stitching";
import { AuthRequest } from "./auth";
import { WebSocketServer } from "ws";
import { Server } from "node:http";
import { useServer } from "graphql-ws/use/ws";
import { ApolloServerPluginDrainHttpServer } from "@apollo/server/plugin/drainHttpServer";

type Handler = (req: Request, res: Response, next: NextFunction) => void;

export const setupApolloServer = async (app: Application, httpServer: Server): Promise<ApolloServer> => {
  const schema = await createFullSchema();

  const wsServer = new WebSocketServer({
    server: httpServer,
    path: "/subscriptions",
  });

  const serverCleanup = useServer(
    {
      schema,
      context: (ctx) => {
        // Forward connectionParams so stitched subscribers can access auth tokens
        return {
          connectionParams: ctx.connectionParams ?? {},
        };
      },
    },
    wsServer,
  );

  const apolloServer = new ApolloServer({
    schema,
    plugins: [
      ApolloServerPluginLandingPageDisabled(),
      ApolloServerPluginDrainHttpServer({ httpServer }),
      {
        async serverWillStart() {
          return {
            async drainServer() {
              await serverCleanup.dispose();
            },
          };
        },
      },
    ],
  });

  await apolloServer.start();

  if (process.env.NODE_ENV === "production") {
    app.use("/graphql", addGraphiqlHeaders);
  }
  app.use(
    "/graphql",
    express.json(),
    expressMiddleware(apolloServer, {
      context: async ({ req }) => req as AuthRequest,
    }),
  );

  return apolloServer;
};

// Just some headers that the special Apollo Server graphiql asked for
const addGraphiqlHeaders: Handler = (req, res, next) => {
  res.setHeader("access-control-allow-origin", "https://studio.apollographql.com");
  res.setHeader("access-control-allow-credentials", "true");
  res.setHeader("access-control-allow-methods", "POST");

  next();
};
