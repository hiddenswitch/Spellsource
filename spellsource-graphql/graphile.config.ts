import PgOmitArchivedPlugin from "@graphile-contrib/pg-omit-archived";
import { PostGraphileConnectionFilterPreset } from "postgraphile-plugin-connection-filter";
import { makeV4Preset } from "postgraphile/presets/v4";
import PostGraphileAmberPreset from "postgraphile/presets/amber";
import { makePgService } from "postgraphile/adaptors/pg";
import { Pool } from "pg";
import {pgDatabase, pgHost, pgPassword, pgPort, pgUser} from "./config";

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
      // watchPg: true, // Need extension for this to work properly
      graphiql: false,
      enhanceGraphiql: false,
      // externalUrlBase: "/api", // Don't use this since graphql route is incorrect w/ it
      graphqlRoute: "/graphql",
      graphiqlRoute: "/graphiql",
      retryOnInitFail: true,
      // retryOnInitFail is mainly so that going to /api/graphiql
      // doesn't crash entire app if config is incorrect. Fix config.
      /*appendPlugins: [PgConnectionFilterPlugin, PgMutationUpsertPlugin, PgOmitArchivedPlugin],*/
      dynamicJson: true,
      graphileBuildOptions: {
        pgArchivedColumnName: "is_archived",
      } as any,
    }),
    PostGraphileConnectionFilterPreset,
  ],
  plugins: [PgOmitArchivedPlugin],
  schema: {
    dontSwallowErrors: true,
    retryOnInitFail: true,
    exportSchemaSDLPath: "schema.graphql",
    pgArchivedColumnName: "is_archived",
    connectionFilterAllowNullInput: true
  },
  pgServices: [
    // @ts-ignore
    makePgService({
      schemas: ["spellsource"],
      pool: pgPool,
      pgSettings: (req: any) => ({
        role: "website",
        "user.id": req.session?.token?.sub ?? "",
      }),
    }),
  ],
};

export default preset;
