import {Pool} from 'pg';
import {postgraphile} from "postgraphile";
import {NextApiRequest, NextApiResponse} from "next";

const pool = new Pool({
  user: "admin",
  password: "password",
  host: "localhost",
  database: "spellsource",
  port: 5432,
});

export {pool as pgPool};

export const postgraphileOptions = {
  // watchPg: true, // Need extension for this to work properly
  graphiql: true,
  enhanceGraphiql: true,
  // externalUrlBase: "/api", // Don't use this since graphql route is incorrect w/ it
  graphqlRoute: "/api/graphql",
  graphiqlRoute: "/api/graphiql",
  retryOnInitFail: true,
  exportGqlSchemaPath: "src/__generated__/schema.graphql"
  // retryOnInitFail is mainly so that going to /api/graphiql
  // doesn't crash entire app if config is incorrect. Fix config.
}
