import Head from "next/head";
import { useEffect, useRef, useState, type FunctionComponent } from "react";

import { resolveLegacyBuild, type UnityBuild } from "../lib/webgl-build";

declare global {
  interface Window {
    createUnityInstance?: (canvas: HTMLCanvasElement, config: Record<string, unknown>,
      onProgress?: (progress: number) => void) => Promise<{ Quit: () => Promise<void> }>;
  }
}

async function resolveBuild(): Promise<UnityBuild> {
  const legacy = process.env.NEXT_PUBLIC_SPELLSOURCE_WEBGL_INDEX;
  if (legacy) return resolveLegacyBuild(legacy);
  const id = new URLSearchParams(location.search).get("buildId") ?? process.env.NEXT_PUBLIC_SPELLSOURCE_WEBGL_BUILD_ID;
  const response = await fetch(`/api/webgl/build${id ? `?buildId=${encodeURIComponent(id)}` : ""}`, { cache: "no-store" });
  if (!response.ok) throw new Error("The browser build is temporarily unavailable. Please retry.");
  return response.json();
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
  const [sessionStatus, setSessionStatus] = useState<string>();

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) {
      return;
    }
    let cancelled = false;
    let instance: { Quit: () => Promise<void> } | undefined;

    const fit = () => {
      canvas.style.width = `${window.visualViewport?.width ?? window.innerWidth}px`;
      canvas.style.height = `${window.visualViewport?.height ?? window.innerHeight}px`;
    };
    const onSession = (event: Event) => {
      const state = (event as CustomEvent<{ phase: string; message?: string }>).detail;
      setSessionStatus(["live", "idle", "ended", "authenticationRequired"].includes(state.phase)
        ? undefined : state.message ?? "Reconnecting…");
      document.documentElement.dataset.gameSession = state.phase;
    };
    const onContextLost = (event: Event) => {
      event.preventDefault();
      setError("The graphics context was interrupted. Reload to restore your session.");
    };
    window.addEventListener("spellsource-session", onSession);
    canvas.addEventListener("webglcontextlost", onContextLost);
    window.visualViewport?.addEventListener("resize", fit);
    window.addEventListener("resize", fit);
    fit();

    (async () => {
      const build = await resolveBuild();
      if (cancelled) return;
      setVersion(build.productVersion);
      document.documentElement.dataset.webglBuild = build.buildId ?? "legacy";
      document.documentElement.dataset.webglRevision = build.sourceRevision ?? "";
      setStatus("Downloading");
      await loadScript(build.loaderUrl);
      if (cancelled || !window.createUnityInstance) return;
      instance = await window.createUnityInstance(
        canvas,
        {
          arguments: [],
          devicePixelRatio: Math.min(window.devicePixelRatio || 1, 2),
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
      if (cancelled) {
        await instance.Quit();
        return;
      }
      setReady(true);
    })().catch((e: unknown) => {
      if (!cancelled) {
        setError(e instanceof Error ? e.message : String(e));
      }
    });

    return () => {
      cancelled = true;
      window.removeEventListener("resize", fit);
      window.visualViewport?.removeEventListener("resize", fit);
      window.removeEventListener("spellsource-session", onSession);
      canvas.removeEventListener("webglcontextlost", onContextLost);
      instance?.Quit().catch(() => undefined);
    };
  }, []);

  return (
    <>
      <Head>
        <title>Spellsource — Play</title>
        <link rel="canonical" href="https://playspellsource.com/game" />
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
          style={{ position: "absolute", top: 0, left: 0, imageRendering: "pixelated", touchAction: "none" }}
        />
        {(!ready || error) && (
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
                <p>Spellsource could not continue.</p>
                <p style={{ opacity: 0.7, fontSize: 14 }}>{error}</p>
                <button onClick={() => location.reload()}>Reload and restore session</button>
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
        {ready && !error && sessionStatus && (
          <div role="status" aria-live="polite" data-testid="game-recovery"
            style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.75)", color: "white",
              display: "grid", placeItems: "center", padding: "max(24px, env(safe-area-inset-top))", touchAction: "none" }}>
            {sessionStatus}
          </div>
        )}
      </main>
    </>
  );
};

export default GamePage;
