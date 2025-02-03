import "./config";
import { createServer } from "node:http";
import express from "express";
import { grafserv } from "postgraphile/grafserv/express/v4";
import { postgraphile } from "postgraphile";
import { graphqlHost, graphqlPort } from "./config";
import preset, { pgPool } from "./graphile.config";
import { authenticate } from "./auth";
import cors from "cors";

(async () => {
  console.log("Starting express server");
  const app = express();
  app.get("/", (req, res) => {
    res.send("Healthy");
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

  const pgl = postgraphile(preset);

  const serv = pgl.createServ(grafserv);

  await serv.addTo(app, server).catch((e) => {
    console.error(e);
    process.exit(1);
  });

  console.log("Postgraphile ready");
})();
