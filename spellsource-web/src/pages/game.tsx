import Head from "next/head";
import type { FunctionComponent } from "react";

const WEBGL_BUILD_VERSION = "4.0.4";
const WEBGL_BUILD_URL =
  `https://s3-builds.appmana.com/spellsource-build-artifacts/spellsource-client/webgl/${WEBGL_BUILD_VERSION}/index.html`;

const GamePage: FunctionComponent = () => (
  <>
    <Head>
      <title>Spellsource — Play</title>
      <meta
        name="description"
        content="Play Spellsource in your browser."
      />
      <meta
        name="viewport"
        content="width=device-width, height=device-height, initial-scale=1, viewport-fit=cover"
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
    <main
      style={{
        position: "fixed",
        inset: 0,
        width: "100vw",
        height: "100dvh",
        overflow: "hidden",
        background: "#000",
      }}
    >
      <iframe
        src={WEBGL_BUILD_URL}
        title="Spellsource"
        allow="autoplay; fullscreen; gamepad"
        allowFullScreen
        style={{
          display: "block",
          width: "100%",
          height: "100%",
          border: 0,
          background: "#000",
        }}
      />
    </main>
  </>
);

export default GamePage;
