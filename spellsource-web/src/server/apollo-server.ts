import { ApolloServer } from "@apollo/server";
import { stitchSchemas } from "@graphql-tools/stitch";
import { createPostgraphileSchema } from "./postgraphile-apollo-server";
import { printSchema } from "graphql";
import fs from "fs";
import { createArtSchema } from "./art";
import { invalidateDeckPlugin } from "../lib/invalidate-deck-plugin";
import preset from "../../graphile.config";

const path = "src/__generated__/schema.graphql";

export const createApolloServer = async () => {
  const postgraphileSchema = await createPostgraphileSchema(preset);

  const artSchema = await createArtSchema();

  const schema = stitchSchemas({ subschemas: [postgraphileSchema, artSchema] });

  if (process.env.VERBOSE) {
    console.log("Successfully stitched schemas");
  }

  if (process.env.NODE_ENV !== "production" && path) {
    const contents = printSchema(schema);
    await fs.promises.writeFile(path, contents);
    console.log(`Wrote schema to ${path}`);
  }

  const server = new ApolloServer({
    schema,
    plugins: [invalidateDeckPlugin],
    introspection: process.env.NODE_ENV !== "production",
  });

  if (process.env.VERBOSE == "true") {
    console.log("Successfully created apollo server");
  }

  return server;
};
