import type { NextApiRequest, NextApiResponse } from "next";
import { WEBGL_ROOT, validBuildId, validateBuild, resolveLegacyBuild } from "../../../lib/webgl-build";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  res.setHeader("Cache-Control", "no-store");
  if (req.method !== "GET") return res.status(405).end();
  const id = req.query.buildId;
  if (id !== undefined && (typeof id !== "string" || !validBuildId(id))) {
    return res.status(400).json({ error: "Invalid build ID" });
  }
  try {
    const response = await fetch(`${WEBGL_ROOT}${id ? `${id}/build.json` : "current.json"}`, {
      cache: "no-store", signal: AbortSignal.timeout(15_000),
    });
    if (response.status === 404 && !id) {
      return res.json(await resolveLegacyBuild(`${WEBGL_ROOT}latest/index.html`));
    }
    if (!response.ok) return res.status(response.status === 404 ? 404 : 502).json({ error: "Browser build unavailable" });
    return res.json(validateBuild(await response.json()));
  } catch {
    return res.status(502).json({ error: "Browser build unavailable. Please retry." });
  }
}
