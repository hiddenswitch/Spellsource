import { ApolloServer } from "@apollo/server";
import { expressMiddleware } from "@apollo/server/express4";
import { ApolloServerPluginLandingPageDisabled } from "@apollo/server/plugin/disabled";
import express, { Application, NextFunction, Request, Response } from "express";
import { createFullSchema } from "./schema/stitching";
import { AuthRequest } from "./auth";
import { WebSocketServer } from "ws";
import { Server } from "node:http";
import { GRAPHQL_TRANSPORT_WS_PROTOCOL } from "graphql-ws";
import { useServer } from "graphql-ws/use/ws";
import { execute, subscribe } from "graphql";
import { GRAPHQL_WS, SubscriptionServer } from "subscriptions-transport-ws";
import { ApolloServerPluginDrainHttpServer } from "@apollo/server/plugin/drainHttpServer";

type Handler = (req: Request, res: Response, next: NextFunction) => void;

export const setupApolloServer = async (app: Application, httpServer: Server): Promise<ApolloServer> => {
  const schema = await createFullSchema();

  // Modern graphql-transport-ws protocol (graphql-ws library)
  const graphqlWsServer = new WebSocketServer({ noServer: true });
  const serverCleanup = useServer({ schema }, graphqlWsServer);

  // Legacy graphql-ws subprotocol (subscriptions-transport-ws library) — for Strawberry Shake clients
  const legacyWsServer = new WebSocketServer({ noServer: true });
  SubscriptionServer.create(
    {
      schema,
      execute: execute as any,
      subscribe: subscribe as any,
    },
    legacyWsServer,
  );

  // Route WebSocket upgrade requests to the correct subprotocol handler
  httpServer.on("upgrade", (req, socket, head) => {
    if (!req.url?.startsWith("/subscriptions")) return;

    const protocolHeader = req.headers["sec-websocket-protocol"];
    const protocols = Array.isArray(protocolHeader)
      ? protocolHeader
      : protocolHeader?.split(",").map((p) => p.trim());

    const wss =
      protocols?.includes(GRAPHQL_WS) && !protocols.includes(GRAPHQL_TRANSPORT_WS_PROTOCOL)
        ? legacyWsServer
        : graphqlWsServer;

    wss.handleUpgrade(req, socket, head, (ws) => {
      wss.emit("connection", ws, req);
    });
  });

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
