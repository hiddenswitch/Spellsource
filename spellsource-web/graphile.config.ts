import { pgPool } from "./src/server/postgres-pool";
import { makeV4Preset } from "postgraphile/presets/v4";
import PostGraphileAmberPreset from "postgraphile/presets/amber";
import { makePgService } from "postgraphile/adaptors/pg";
import PgOmitArchivedPlugin from "@graphile-contrib/pg-omit-archived";
import { PostGraphileConnectionFilterPreset } from "postgraphile-plugin-connection-filter";

const preset: GraphileConfig.Preset = {
  extends: [
    PostGraphileAmberPreset,
    makeV4Preset({
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
      /*appendPlugins: [PgConnectionFilterPlugin, PgMutationUpsertPlugin, PgOmitArchivedPlugin],*/
      dynamicJson: true,
      graphileBuildOptions: {
        pgArchivedColumnName: "is_archived",
      } as any,
    }),
    PostGraphileConnectionFilterPreset,
  ],
  plugins: [PgOmitArchivedPlugin],
  pgServices: [
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
