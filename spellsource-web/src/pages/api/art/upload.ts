import { NextApiRequest, NextApiResponse } from "next";
import { getSessionDirect } from "../auth/[...nextauth]";
import { PutObjectCommand, S3Client } from "@aws-sdk/client-s3";
import { awsAccessKeyId, awsBucketName, awsSecretAccessKey } from "../../../lib/config";
import { randomUUID } from "crypto";
import getRawBody from "raw-body";

const s3 = new S3Client({
  region: "us-west-2",
  credentials: {
    accessKeyId: awsAccessKeyId,
    secretAccessKey: awsSecretAccessKey,
  },
});

export const config = {
  api: {
    bodyParser: false, // so we can pass the raw buffer to s3
  },
};

export default async (req: NextApiRequest, res: NextApiResponse) => {
  const session = await getSessionDirect(req);

  const userId = session?.token?.sub;

  if (!userId) {
    res.status(401).json("Must be logged in");
    return;
  }

  const uuid = randomUUID();

  try {
    await s3.send(
      new PutObjectCommand({
        Bucket: awsBucketName,
        Key: `images/uploaded/${userId}/${uuid}`,
        ContentType: req.headers["content-type"],
        Body: await getRawBody(req),
      })
    );

    res.status(200).json(uuid);
  } catch (e: any) {
    res.status(e?.["$metadata"]?.["httpStatusCode"] || 500).json(e);
  }
};
