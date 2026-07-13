import "./config";
import { createServer } from "node:http";
import express from "express";
import { graphqlHost, graphqlPort } from "./config";
import { pgPool } from "./graphile.config";
import { authenticate } from "./auth";
import cors from "cors";
import { setupApolloServer } from "./apollo-server";

(async () => {
  console.log("Starting express server");
  const app = express();
  let ready = false;
  app.get("/", (req, res) => {
    res.send("Healthy");
  });
  app.get("/readiness", (req, res) => {
    if (ready) {
      res.send("Ready");
    } else {
      res.status(503).send("GraphQL schema is not ready");
    }
  });

  if (process.env.NODE_ENV !== "production") {
    app.use(cors());
  }

  app.use(`/graphql`, authenticate);

  const server = createServer(app);
  server.on("error", () => {});
  server.listen(graphqlPort, graphqlHost);
  console.log(`Server listening at http://${graphqlHost}:${graphqlPort}`);

  // Ensure schema ready
  while (true) {
    try {
      await pgPool.query(`
        select success
        from hiddenswitch.flyway_schema_history
        order by installed_rank desc
        limit 1
      `);
      break;
    } catch (e) {
      console.log("Schema not ready yet, retrying in 10s");
      await new Promise((resolve) => setTimeout(resolve, 10e3));
    }
  }

  await setupApolloServer(app, server);
  ready = true;

  console.log("Postgraphile ready");
})();
