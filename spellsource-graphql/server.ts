import "./config";
import {createServer} from "node:http";
import express from "express";
import {grafserv} from "postgraphile/grafserv/express/v4";
import {postgraphile} from "postgraphile";
import {graphqlHost, graphqlPort} from "./config";
import preset from "./graphile.config";

(async () => {

  const app = express();
  app.get("/", (req, res) => {
    res.send("Healthy");
  });
  
  const server = createServer(app);
  server.on("error", () => {
  });
  server.listen(graphqlPort, graphqlHost);
  console.log(`Server listening at http://${graphqlHost}:${graphqlPort}`);
  
  const sleep = parseInt(process.env.SLEEP || "0")
  if (sleep > 0) {
    console.log(`Waiting ${sleep} seconds to start postgraphile introspection`)
    await new Promise(resolve => setTimeout(resolve, sleep * 1000));
  }

  // Our PostGraphile instance:
  const pgl = postgraphile(preset);

  const serv = pgl.createServ(grafserv);
  
  await serv.addTo(app, server).catch((e) => {
    console.error(e);
    process.exit(1);
  });
  
  console.log("Postgraphile ready")
})()
