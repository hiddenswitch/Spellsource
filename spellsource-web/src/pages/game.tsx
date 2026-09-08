import Head from "next/head";
import { useEffect, useRef, useState, type FunctionComponent } from "react";

/**
 * The published Unity WebGL client. `latest/index.html` redirects to the current
 * versioned build, whose index.html carries the hashed asset names.
 */
const WEBGL_LATEST_URL =
  "https://s3-builds.appmana.com/spellsource-build-artifacts/spellsource-client/webgl/latest/index.html";

type UnityBuild = {
  baseUrl: string;
  loaderUrl: string;
  dataUrl: string;
  frameworkUrl: string;
  codeUrl: string;
  symbolsUrl?: string;
  productVersion?: string;
};

declare global {
  interface Window {
    createUnityInstance?: (
      canvas: HTMLCanvasElement,
      config: Record<string, unknown>,
      onProgress?: (progress: number) => void
    ) => Promise<{ Quit: () => Promise<void> }>;
  }
}

function attr(html: string, name: string): string | undefined {
  return html.match(new RegExp(`${name}:\\s*"([^"]+)"`))?.[1];
}

/**
 * Resolves the current build by following the latest redirect and reading the
 * asset URLs out of Unity's generated index.html, so the page tracks whatever
 * the build pipeline last published without a manifest.
 */
async function resolveBuild(): Promise<UnityBuild> {
  const latest = await fetch(WEBGL_LATEST_URL).then((r) => r.text());
  const target = latest.match(/url=([^"\s]+)/)?.[1];
  if (!target) {
    throw new Error("could not find the latest client build");
  }
  const indexUrl = new URL(target, WEBGL_LATEST_URL).toString();
  const baseUrl = indexUrl.replace(/index\.html$/, "");
  const html = await fetch(indexUrl).then((r) => r.text());
  const loader = html.match(/<script src="([^"]+\.loader\.js)"/)?.[1];
  const dataUrl = attr(html, "dataUrl");
  const frameworkUrl = attr(html, "frameworkUrl");
  const codeUrl = attr(html, "codeUrl");
  if (!loader || !dataUrl || !frameworkUrl || !codeUrl) {
    throw new Error("client build index is missing asset URLs");
  }
  const symbolsUrl = attr(html, "symbolsUrl");
  return {
    baseUrl,
    loaderUrl: baseUrl + loader,
    dataUrl: baseUrl + dataUrl,
    frameworkUrl: baseUrl + frameworkUrl,
    codeUrl: baseUrl + codeUrl,
    symbolsUrl: symbolsUrl ? baseUrl + symbolsUrl : undefined,
    productVersion: attr(html, "productVersion"),
  };
}

function loadScript(src: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = src;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error(`failed to load ${src}`));
    document.body.appendChild(script);
  });
}

const GamePage: FunctionComponent = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState("Finding the latest build");
  const [version, setVersion] = useState<string>();
  const [error, setError] = useState<string>();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) {
      return;
    }
    let cancelled = false;
    let instance: { Quit: () => Promise<void> } | undefined;

    const fit = () => {
      canvas.style.width = `${window.innerWidth}px`;
      canvas.style.height = `${window.innerHeight}px`;
    };
    window.addEventListener("resize", fit);
    fit();

    (async () => {
      const build = await resolveBuild();
      if (cancelled) return;
      setVersion(build.productVersion);
      setStatus("Downloading");
      await loadScript(build.loaderUrl);
      if (cancelled || !window.createUnityInstance) return;
      instance = await window.createUnityInstance(
        canvas,
        {
          arguments: [],
          dataUrl: build.dataUrl,
          frameworkUrl: build.frameworkUrl,
          codeUrl: build.codeUrl,
          symbolsUrl: build.symbolsUrl,
          streamingAssetsUrl: build.baseUrl + "StreamingAssets",
          companyName: "Hidden Switch",
          productName: "Spellsource",
          productVersion: build.productVersion,
          // The client keeps the signed-in account (guests included) in
          // Application.persistentDataPath, which on WebGL is an in-memory file
          // system mirrored to IndexedDB only when synced. The build never syncs
          // it, so without this a refresh forgets the guest and the match it is in.
          autoSyncPersistentDataPath: true,
        },
        (p) => {
          if (cancelled) return;
          setProgress(p);
          // The loader reports 0.9 once assets are down and holds there while
          // the engine boots; the first frame follows shortly after.
          setStatus(p < 0.9 ? "Downloading" : "Starting");
        }
      );
      if (cancelled) return;
      setReady(true);
    })().catch((e: unknown) => {
      if (!cancelled) {
        setError(e instanceof Error ? e.message : String(e));
      }
    });

    return () => {
      cancelled = true;
      window.removeEventListener("resize", fit);
      instance?.Quit().catch(() => undefined);
    };
  }, []);

  return (
    <>
      <Head>
        <title>Spellsource — Play</title>
        <meta name="description" content="Play Spellsource in your browser." />
        <meta
          name="viewport"
          content="width=device-width, height=device-height, initial-scale=1, user-scalable=no, viewport-fit=cover"
        />
      </Head>
      <style jsx global>{`
        html,
        body,
        #__next {
          width: 100%;
          height: 100%;
          margin: 0;
          overflow: hidden;
          background: #000;
        }
      `}</style>
      <main style={{ position: "fixed", inset: 0, background: "#000" }}>
        <canvas
          ref={canvasRef}
          id="unity-canvas"
          width={1280}
          height={720}
          tabIndex={-1}
          style={{ position: "absolute", top: 0, left: 0, imageRendering: "pixelated" }}
        />
        {!ready && (
          <div
            style={{
              position: "absolute",
              inset: 0,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              gap: 24,
              background: "linear-gradient(180deg, #1d1e5b 0%, #341f42 100%)",
              color: "#fff",
              fontFamily: "sans-serif",
              textAlign: "center",
            }}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/static/assets/spellsource.png" alt="Spellsource" style={{ width: "min(70vw, 480px)" }} />
            {error ? (
              <div style={{ maxWidth: 480, padding: "0 24px" }}>
                <p>Spellsource could not start.</p>
                <p style={{ opacity: 0.7, fontSize: 14 }}>{error}</p>
              </div>
            ) : (
              <>
                <div
                  role="progressbar"
                  aria-valuemin={0}
                  aria-valuemax={100}
                  aria-valuenow={Math.round(progress * 100)}
                  style={{
                    width: "min(60vw, 400px)",
                    height: 12,
                    border: "2px solid #fab982",
                    borderRadius: 6,
                    overflow: "hidden",
                    background: "rgba(0,0,0,0.4)",
                  }}
                >
                  <div
                    style={{
                      width: `${Math.round(progress * 100)}%`,
                      height: "100%",
                      background: "#fab982",
                      transition: "width 200ms linear",
                    }}
                  />
                </div>
                <div style={{ fontSize: 14, opacity: 0.85 }}>
                  {status}
                  {status === "Downloading" ? ` ${Math.round(progress * 100)}%` : ""}
                  {version ? ` · v${version}` : ""}
                </div>
              </>
            )}
          </div>
        )}
      </main>
    </>
  );
};

export default GamePage;
