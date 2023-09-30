import { startServerAndCreateNextHandler } from "@as-integrations/next";
import { createApolloServer } from "../../server/apollo-server";
import { getSessionDirect } from "./auth/[...nextauth]";
import Cors from "cors";
import { NextApiRequest, NextApiResponse } from "next";

const cors = Cors({
  methods: ["POST", "GET", "HEAD"],
});

export default async (req: NextApiRequest, res: NextApiResponse) => {
  const process1 = process as any;
  process1["handler"] ??= createApolloServer().then((server) =>
    startServerAndCreateNextHandler(server, {
      context: async (req) => {
        const session = await getSessionDirect(req);
        return { session };
      },
    })
  );

  if (process1.env.NODE_ENV !== "production") {
    await runMiddleware(req, res, cors);
  }

  return (await process1["handler"])(req, res);
};

function runMiddleware(req: NextApiRequest, res: NextApiResponse, fn: Function) {
  return new Promise((resolve, reject) => {
    fn(req, res, (result: any) => {
      if (result instanceof Error) {
        return reject(result);
      }

      return resolve(result);
    });
  });
}
