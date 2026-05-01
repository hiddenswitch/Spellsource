import { ApolloServer } from "@apollo/server";
import { expressMiddleware } from "@apollo/server/express4";
import {
  ApolloServerPluginLandingPageLocalDefault,
  ApolloServerPluginLandingPageProductionDefault,
} from "@apollo/server/plugin/landingPage/default";
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

  // Strawberry Shake passes the auth token via `?accessToken=<jwt>` on the WS URL
  // (see SpellsourceClient.ConfigureWebSocketClient on the client). Extract it during
  // upgrade so subscription operations can forward it to the Java backend.
  const tokenFromUrl = (url: string | undefined): string | undefined => {
    if (!url) return undefined;
    const parsed = new URL(url, "http://localhost");
    const token = parsed.searchParams.get("accessToken");
    return token && token !== "0" ? token : undefined;
  };

  // Modern graphql-transport-ws protocol (graphql-ws library)
  const graphqlWsServer = new WebSocketServer({ noServer: true });
  const serverCleanup = useServer(
    {
      schema,
      // The context returned here is passed as `executionRequest.context` to
      // schema executors — including the wsExecutor in spellsource.ts which
      // reads `token` to forward auth to the Java backend's connection_init.
      context: (ctx) => ({ token: tokenFromUrl(ctx.extra.request.url) }),
    },
    graphqlWsServer,
  );

  // Legacy graphql-ws subprotocol (subscriptions-transport-ws library) — for Strawberry Shake clients
  const legacyWsServer = new WebSocketServer({ noServer: true });
  SubscriptionServer.create(
    {
      schema,
      execute: execute as any,
      subscribe: subscribe as any,
      onConnect: (_params: any, _ws: any, connectionContext: any) => {
        // onConnect's return value becomes the operation context for execute/subscribe.
        const token = tokenFromUrl(connectionContext?.request?.url);
        console.log(`[legacy-ws] client connected, hasToken=${!!token}`);
        return { token };
      },
      onOperation: (_msg: any, params: any) => {
        console.log("[legacy-ws] operation started:", JSON.stringify(params.query?.substring(0, 200)));
        return params;
      },
      onDisconnect: () => {
        console.log("[legacy-ws] client disconnected");
      },
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

    console.log(`[ws-upgrade] protocols requested: ${JSON.stringify(protocols)}`);

    const wss =
      protocols?.includes(GRAPHQL_WS) && !protocols.includes(GRAPHQL_TRANSPORT_WS_PROTOCOL)
        ? legacyWsServer
        : graphqlWsServer;

    console.log(`[ws-upgrade] routing to: ${wss === legacyWsServer ? "legacy" : "modern"}`);

    wss.handleUpgrade(req, socket, head, (ws) => {
      // Log raw messages from the client
      ws.on("message", (data) => {
        console.log(`[ws-message] ${data.toString().substring(0, 500)}`);
      });
      wss.emit("connection", ws, req);
    });
  });

  const apolloServer = new ApolloServer({
    schema,
    plugins: [
      process.env.NODE_ENV === "production"
        ? ApolloServerPluginLandingPageProductionDefault()
        : ApolloServerPluginLandingPageLocalDefault({ embed: true }),
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
    express.json({ limit: "10mb" }),
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
