import { expect, test } from "@playwright/test";
import { Resolver } from "node:dns/promises";
import https from "node:https";
import { siteHosts } from "./hosts";

// resolve through public resolvers on purpose: inside the network split-horizon
// dns answers with internal addresses and would hide a broken public zone
const resolver = new Resolver();
resolver.setServers(["1.1.1.1", "8.8.8.8"]);

function fetchViaAddress(host: string, address: string): Promise<number> {
  return new Promise((resolve, reject) => {
    const req = https.request(
      { host: address, servername: host, headers: { Host: host }, path: "/", method: "GET", timeout: 15000 },
      (res) => {
        res.resume();
        resolve(res.statusCode ?? 0);
      }
    );
    req.on("timeout", () => req.destroy(new Error(`timed out connecting to ${address} for ${host}`)));
    req.on("error", reject);
    req.end();
  });
}

test.describe("public dns", () => {
  test("every public host resolves to one address that serves it", async () => {
    const addresses = new Map<string, string[]>();
    for (const host of siteHosts) {
      addresses.set(host, (await resolver.resolve4(host)).sort());
    }
    for (const [host, found] of addresses) {
      expect(found, `${host} has no public A record`).not.toHaveLength(0);
    }
    const distinct = new Set([...addresses.values()].map((a) => a.join(",")));
    expect(distinct.size, `hosts disagree on their address: ${JSON.stringify(Object.fromEntries(addresses))}`).toBe(1);

    // the address in public dns must actually answer with a valid certificate
    // for each name; a stale record pointing at an old wan address fails here
    for (const [host, [address]] of addresses) {
      const status = await fetchViaAddress(host, address);
      expect(status, `${host} at ${address}`).toBeGreaterThanOrEqual(200);
      expect(status, `${host} at ${address}`).toBeLessThan(500);
    }
  });
});
