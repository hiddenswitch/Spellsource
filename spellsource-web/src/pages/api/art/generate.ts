import { NextApiRequest, NextApiResponse } from "next";
import { getSessionDirect } from "../auth/[...nextauth]";
import { PutObjectCommand, S3Client } from "@aws-sdk/client-s3";
import { awsAccessKeyId, awsBucketName, awsSecretAccessKey, comfyUrl } from "../../../lib/config";
import sdxl from "../../../__generated__/sdxl-ordinary.json";
import { PromptNode } from "../../../__generated__/comfyclient";
import fetch from "node-fetch";
import { GraphQLError } from "graphql/index";
import { MutationGenerateArtArgs } from "../../../lib/art-generation";

const s3 = new S3Client({
  region: "us-west-2",
  credentials: {
    accessKeyId: awsAccessKeyId,
    secretAccessKey: awsSecretAccessKey,
  },
});

export const getPrompt = (args: MutationGenerateArtArgs) => {
  const { positiveText, negativeText, seed } = args;

  const promptText = JSON.stringify(sdxl).replace("$POSITIVE_TEXT", positiveText.trim()).replace("$NEGATIVE_TEXT", negativeText.trim());

  const prompt = JSON.parse(promptText) as Record<string, PromptNode>;

  for (let node of Object.values(prompt)) {
    if (node.class_type === "KSampler") {
      node.inputs["seed"] = seed!;
    }
  }

  return prompt;
};

export const generateArt = async (args: MutationGenerateArtArgs) => {
  const prompt = getPrompt(args);

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

  let relativePath = "/";
  try {
    const parsedUrl = new URL(urls[0]);
    relativePath = parsedUrl.pathname + parsedUrl.search;
  } catch {
    relativePath = urls[0];
  }

  if (!relativePath) {
    console.warn(comfyResponse);
    throw new GraphQLError("Failed to retrieve generated image");
  }

  const image = await fetch(comfyUrl + relativePath);

  const body = Buffer.from(await image.arrayBuffer());
  const contentType = comfyResponse.headers.get("content-type") || undefined;

  const path = relativePath.replace("/api/v1/", "");

  try {
    // only save to s3 if we've configured credentials
    if (awsAccessKeyId) {
      const result = await s3.send(
        new PutObjectCommand({
          Bucket: awsBucketName,
          Key: path,
          Body: body,
          ContentType: contentType,
        })
      );
    }
  } catch (e: unknown) {
    console.error(e);
  }

  return urls;
};

export default async (req: NextApiRequest, res: NextApiResponse) => {
  const session = await getSessionDirect(req);

  const userId = session?.token?.sub;

  if (!userId) {
    res.status(401).json("Must be logged in");
    return;
  }

  const result = await generateArt(req.body);

  res.status(200).json(result);
};
