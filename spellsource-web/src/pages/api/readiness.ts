import { graphqlHost, keycloakUrl } from "../../lib/config";
import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const keycloak = await fetch(keycloakUrl, { redirect: "follow" });

  if (!keycloak.ok) {
    console.error(`Tried to reach keycloak at ${keycloakUrl} but couldn't`);
    res.status(500).send("Can't reach keycloak");
    return;
  }

  const graphql = await fetch(graphqlHost, { redirect: "follow" });
  if (!graphql.ok) {
    console.error(`Tried to reach graphql at ${graphqlHost} but couldn't`);
    res.status(500).send("Can't reach graphql");
    return;
  }
}
