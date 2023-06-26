import { makeExecutableSchema } from "@graphql-tools/schema";
import { gql } from "@apollo/client";
import { readAllImages } from "../lib/fs-utils";
import path from "path";
import { keyBy } from "lodash";
import { ImageDef, QueryArtByIdArgs } from "../__generated__/client";
import { GraphQLResolveInfo } from "graphql";

const typeDefs = gql`
  type ImageDef {
    id: String!
    name: String!
    src: String!
    height: Int!
    width: Int!
  }

  type Query {
    artById(id: String!): ImageDef
    allArt: [ImageDef!]!
  }
`;

const resolvers = {
  Query: {
    artById: async (parent, args: QueryArtByIdArgs, context, info: GraphQLResolveInfo) =>
      (await getAllArt())?.[args.id],
    allArt: async (parent, args, context, info: GraphQLResolveInfo) => Object.values(await getAllArt()),
  },
};

let artById: Promise<Record<string, ImageDef>>;

export const getAllArt = async () => {
  if (!artById) {
    console.log("Reading art from disk");
    artById = readAllImages(path.join("card-images", "art", "**", "*.png")).then((value) =>
      keyBy(value, (value) => value.name)
    );
  }

  return artById;
};

export const createArtSchema = async () => {
  await getAllArt();
  return makeExecutableSchema({ typeDefs, resolvers });
};
