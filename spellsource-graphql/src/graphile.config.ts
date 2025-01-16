import PgOmitArchivedPlugin from "@graphile-contrib/pg-omit-archived";
import { PostGraphileConnectionFilterPreset } from "postgraphile-plugin-connection-filter";
import { makeV4Preset } from "postgraphile/presets/v4";
import PostGraphileAmberPreset from "postgraphile/presets/amber";
import { makePgService } from "postgraphile/adaptors/pg";
import { Pool } from "pg";
import { pgDatabase, pgHost, pgPassword, pgPort, pgUser } from "./config";
import { AuthRequest } from "./auth";

export const pgPool = new Pool({
  user: pgUser,
  password: pgPassword,
  host: pgHost,
  database: pgDatabase,
  port: pgPort,
  connectionTimeoutMillis: 1000, // Set connection timeout to 1 second
});

const preset: GraphileConfig.Preset = {
  extends: [
    PostGraphileAmberPreset,
    makeV4Preset({
      dynamicJson: true,
      subscriptions: true,
    }),
    PostGraphileConnectionFilterPreset,
  ],
  plugins: [PgOmitArchivedPlugin],
  grafserv: {
    graphiql: true,
  },
  schema: {
    dontSwallowErrors: true,
    retryOnInitFail: true,
    exportSchemaSDLPath: "schema.graphql",
    pgArchivedColumnName: "is_archived",
    connectionFilterAllowNullInput: true,
  },
  pgServices: [
    makePgService({
      schemas: ["spellsource"],
      pubsub: true,
      pool: pgPool,
      pgSettings: ({ expressv4 }) => {
        const req = expressv4.req as AuthRequest;
        return {
          role: req.admin ? "admin" : "website", // TODO use different roles for website / client / server
          "user.id": req.auth?.sub ?? "",
        };
      },
    }),
  ],
};

export default preset;
