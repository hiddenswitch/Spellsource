import {ApolloServer} from "@apollo/server";
import {stitchSchemas} from "@graphql-tools/stitch";
import {makeSchemaAndPlugin} from "./postgraphile-apollo-server";
import {pgPool, postgraphileOptions} from "./postgraphile";
import {printSchema} from "graphql";
import fs from "fs";
import {createArtSchema} from "./art";

const path = postgraphileOptions.exportGqlSchemaPath;

export const createApolloServer = async () => {
  const postgraphile = await makeSchemaAndPlugin(pgPool, "spellsource", postgraphileOptions)

  const artSchema = await createArtSchema();

  const schema = stitchSchemas({subschemas: [postgraphile.schema, artSchema]})

  if (process.env.NODE_ENV !== "production" && path) {
    const contents = printSchema(schema);
    await fs.promises.writeFile(path, contents);
    console.log(`Wrote schema to ${path}`);
  }

  return new ApolloServer({schema, plugins: [postgraphile.plugin]});
}

