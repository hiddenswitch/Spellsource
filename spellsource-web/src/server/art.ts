import { makeExecutableSchema } from "@graphql-tools/schema";
import { gql } from "@apollo/client";
import { getAllArt } from "../lib/fs-utils";
import { keyBy } from "lodash";
import { ImageDef, MutationGenerateArtArgs, QueryArtByIdArgs } from "../__generated__/client";
import { GraphQLError, GraphQLResolveInfo } from "graphql";
import sdxl from "../__generated__/sdxl-ordinary.json";
import { PromptNode } from "../__generated__/comfyclient/models/PromptNode";
import { PutObjectCommand, S3Client } from "@aws-sdk/client-s3";
import fetch from "node-fetch";
import { awsAccessKeyId, awsBucketName, awsSecretAccessKey, comfyUrl } from "../lib/config";

const s3 = new S3Client({
  region: "us-west-2",
  credentials: {
    accessKeyId: awsAccessKeyId,
    secretAccessKey: awsSecretAccessKey,
  },
});

const typeDefs = gql`
  type ImageDef {
    id: String!
    name: String!
    src: String!
    height: Int!
    width: Int!
  }

  type GenerateArtResult {
    urls: [String!]!
  }

  type Query {
    artById(id: String!): ImageDef
    allArt: [ImageDef!]!
  }

  type Mutation {
    generateArt(positiveText: String!, negativeText: String!, seed: Int!): GenerateArtResult
  }
`;

const resolvers = {
  Query: {
    artById: async (parent: unknown, args: QueryArtByIdArgs, context: unknown, info: GraphQLResolveInfo) =>
      (await getArtById())?.[args.id],
    allArt: async (parent: unknown, args: unknown, context: unknown, info: GraphQLResolveInfo) =>
      Object.values(await getArtById()),
  },
  Mutation: {
    generateArt: async (parent: unknown, args: MutationGenerateArtArgs, context: unknown, info: GraphQLResolveInfo) => {
      const { positiveText, negativeText, seed } = args;

      const promptText = JSON.stringify(sdxl)
        .replace("$POSITIVE_TEXT", positiveText.trim())
        .replace("$NEGATIVE_TEXT", negativeText.trim());

      const prompt = JSON.parse(promptText) as Record<string, PromptNode>;

      for (let node of Object.values(prompt)) {
        if (node.class_type === "KSampler") {
          node.inputs["seed"] = seed!;
        }
      }

      const comfyResponse = await fetch(comfyUrl + "/api/v1/prompts", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(prompt),
      });

      const comfyResult = (await comfyResponse.json()) as any;

      if (!comfyResponse.ok || !comfyResult) {
        console.warn(comfyResponse);
        throw new GraphQLError("Failed to generate image");
      }

      const urls = comfyResult["urls"] as string[];
      const [relativePath] = urls;

      if (!relativePath) {
        console.warn(comfyResponse);
        throw new GraphQLError("Failed to retrieve generated image");
      }

      const image = await fetch(comfyUrl + relativePath);

      const body = Buffer.from(await image.arrayBuffer());
      const contentType = comfyResponse.headers.get("content-type") || undefined;

      const path = relativePath.replace("/api/v1/", "");

      const result = await s3.send(
        new PutObjectCommand({
          Bucket: awsBucketName,
          Key: path,
          Body: body,
          ContentType: contentType,
        })
      );

      return { urls };
    },
  },
};

let artById: Promise<Record<string, ImageDef>>;

export const getArtById = async () => {
  if (!artById) {
    console.log("Reading art from disk");
    artById = getAllArt().then((value) => keyBy(value, (value) => value.name));
  }

  return artById;
};

export const createArtSchema = async () => {
  await getArtById();
  return makeExecutableSchema({ typeDefs, resolvers });
};
