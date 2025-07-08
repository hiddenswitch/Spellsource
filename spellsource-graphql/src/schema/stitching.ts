import { createSpellsourceSchema } from "./spellsource";
import { stitchSchemas } from "@graphql-tools/stitch";
import { printSchema } from "graphql/utilities";
import { writeFile } from "node:fs/promises";
import {createPostgraphileSchema} from "./postgraphile";

export const createFullSchema = async () => {
  const postgraphile = await createPostgraphileSchema();

  const spellsource = await createSpellsourceSchema();


  const schema = stitchSchemas({
    subschemas: [postgraphile, spellsource],
    typeMergingOptions: {},
  });


  if (process.env.NODE_ENV !== "production") {
    await writeFile("schema.graphql", printSchema(schema));
  }

  return schema;
};