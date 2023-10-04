import { GetObjectCommand, S3Client } from "@aws-sdk/client-s3";
import { awsAccessKeyId, awsBucketName, awsSecretAccessKey } from "../../../../lib/config";
import { NextApiRequest, NextApiResponse } from "next";

const s3 = new S3Client({
  region: "us-west-2",
  credentials: {
    accessKeyId: awsAccessKeyId,
    secretAccessKey: awsSecretAccessKey,
  },
});

export default async (req: NextApiRequest, res: NextApiResponse) => {
  const hash = req.query["hash"];

  try {
    const data = await s3.send(new GetObjectCommand({ Bucket: awsBucketName, Key: `images/${hash}` }));

    res
      .status(200)
      .setHeader("Content-Type", data.ContentType ?? "")
      .send(data.Body);
  } catch (e: any) {
    res.status(e?.["$metadata"]?.["httpStatusCode"] || 500).json(e);
  }
};
