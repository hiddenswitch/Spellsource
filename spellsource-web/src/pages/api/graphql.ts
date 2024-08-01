import { startServerAndCreateNextHandler } from "@as-integrations/next";
import { createApolloServer } from "../../server/apollo-server";
import { getSessionDirect } from "./auth/[...nextauth]";
import Cors from "cors";
import { NextApiRequest, NextApiResponse } from "next";

const cors = Cors({
  methods: ["POST", "GET", "HEAD"],
});

let handler;

export default async (req: NextApiRequest, res: NextApiResponse) => {
  handler ??= createApolloServer().then((server) =>
    startServerAndCreateNextHandler(server, {
      context: async (req) => {
        const session = await getSessionDirect(req);
        return { session };
      },
    })
  );

  if (process.env.NODE_ENV !== "production") {
    await runMiddleware(req, res, cors);
  }

  (await handler)(req, res);
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
