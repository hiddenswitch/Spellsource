import { Pool } from "pg";
import { pgPort } from "../lib/config";
import { PostGraphileOptions } from "postgraphile";
import PgConnectionFilterPlugin from "postgraphile-plugin-connection-filter";
import { PgMutationUpsertPlugin } from "postgraphile-upsert-plugin";
import PgOmitArchivedPlugin from "@graphile-contrib/pg-omit-archived";

const pool = new Pool({
  user: "admin",
  password: "password",
  host: "localhost",
  database: "spellsource",
  port: pgPort,
});

export { pool as pgPool };

export const postgraphileOptions: PostGraphileOptions = {
  // watchPg: true, // Need extension for this to work properly
  graphiql: false,
  enhanceGraphiql: false,
  // externalUrlBase: "/api", // Don't use this since graphql route is incorrect w/ it
  graphqlRoute: "/api/graphql",
  graphiqlRoute: "/api/graphiql",
  retryOnInitFail: true,
  exportGqlSchemaPath: "src/__generated__/schema.graphql",
  // retryOnInitFail is mainly so that going to /api/graphiql
  // doesn't crash entire app if config is incorrect. Fix config.
  appendPlugins: [PgConnectionFilterPlugin, PgMutationUpsertPlugin, PgOmitArchivedPlugin],
  dynamicJson: true,
  pgSettings: (req: any) => ({
    role: "website",
    "user.id": req.context.session?.token?.sub ?? "",
  }),
  graphileBuildOptions: {
    pgArchivedColumnName: "is_archived",
  },
};
