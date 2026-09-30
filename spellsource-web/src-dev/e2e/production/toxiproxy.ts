import { execFileSync } from "node:child_process";

/**
 * A toxiproxy in Docker that fronts the gateway as a TLS passthrough on
 * 127.0.0.1:443. The browser is launched with host-resolver rules mapping the
 * gateway hosts to 127.0.0.1, so its TLS handshakes still carry the real
 * hostnames and certificates validate; only the TCP path is ours to break.
 */
export const CONTAINER = "spellsource-toxiproxy";
export const API = "http://127.0.0.1:8474";
export const PROXY = "gateway";
const hostNetwork = process.platform === "linux";

type Toxic = { name: string; type: string; stream: "upstream" | "downstream"; toxicity?: number; attributes: Record<string, number> };

function docker(args: string[]): string {
  return execFileSync("docker", args, { encoding: "utf8", stdio: ["ignore", "pipe", "pipe"] }).trim();
}

export function dockerAvailable(): boolean {
  try {
    docker(["version", "--format", "{{.Server.Version}}"]);
    return true;
  } catch {
    return false;
  }
}

async function api(method: string, path: string, body?: unknown) {
  const response = await fetch(API + path, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok && response.status !== 404) {
    throw new Error(`toxiproxy ${method} ${path} -> ${response.status} ${await response.text()}`);
  }
  return response;
}

export async function startToxiproxy(upstreamAddress: string) {
  const running = docker(["ps", "--filter", `name=^${CONTAINER}$`, "--format", "{{.Names}}"]);
  if (running !== CONTAINER) {
    try {
      docker(["rm", "-f", CONTAINER]);
    } catch {
      // not present
    }
    // Avoid adding/removing a Docker bridge interface while Chrome is running:
    // its network notifier can fail unrelated page loads with ERR_NETWORK_CHANGED.
    // Keep both listeners on loopback when sharing the Linux host network.
    const networkArgs = hostNetwork ? ["--network", "host"] : ["-p", "127.0.0.1:8474:8474", "-p", "127.0.0.1:443:443"];
    docker(["run", "-d", "--name", CONTAINER, ...networkArgs, "ghcr.io/shopify/toxiproxy:latest", `-host=${hostNetwork ? "127.0.0.1" : "0.0.0.0"}`]);
  }
  const deadline = Date.now() + 15_000;
  while (Date.now() < deadline) {
    try {
      await api("GET", "/version");
      break;
    } catch {
      await new Promise((r) => setTimeout(r, 250));
    }
  }
  await api("DELETE", `/proxies/${PROXY}`);
  await api("POST", "/proxies", { name: PROXY, listen: hostNetwork ? "127.0.0.1:443" : "0.0.0.0:443", upstream: `${upstreamAddress}:443`, enabled: true });
}

export function stopToxiproxy() {
  try {
    docker(["rm", "-f", CONTAINER]);
  } catch {
    // already gone
  }
}

export async function addToxic(toxic: Toxic) {
  await api("POST", `/proxies/${PROXY}/toxics`, toxic);
}

export async function removeToxic(name: string) {
  await api("DELETE", `/proxies/${PROXY}/toxics/${name}`);
}

export async function clearToxics() {
  const response = await api("GET", `/proxies/${PROXY}/toxics`);
  const toxics = (await response.json()) as Toxic[];
  for (const toxic of toxics) {
    await removeToxic(toxic.name);
  }
  await setEnabled(true);
}

/** Disabling a proxy closes every live connection and refuses new ones. */
export async function setEnabled(enabled: boolean) {
  await api("POST", `/proxies/${PROXY}`, { enabled });
}

/** How many client connections the proxy has accepted, from its log; proves the browser's traffic really goes through it. */
export function acceptedClients(): number {
  const logs = docker(["logs", CONTAINER]);
  return logs.split("\n").filter((line) => line.includes('"message":"Accepted client"')).length;
}

/** Chromium flag that sends the gateway hosts to the proxy. */
export function hostResolverRules(hosts: string[]): string {
  return "--host-resolver-rules=" + hosts.map((h) => `MAP ${h} 127.0.0.1`).join(", ");
}
