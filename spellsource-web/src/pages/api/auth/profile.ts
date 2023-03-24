import type {NextApiRequest, NextApiResponse} from "next";
import {baseUrl, clientId, issuer} from "../../../lib/config";

export default (req: NextApiRequest, res: NextApiResponse) => {
  res.redirect(
    `${issuer}/account?referrer=${clientId}&referrer_uri=${encodeURIComponent(req.headers.referer ?? baseUrl)}`
  );
};
