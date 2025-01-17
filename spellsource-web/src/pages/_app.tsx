import { AppProps } from "next/app";
import "../global.scss";
import "../blockly.scss";
import { ApolloClientProvider } from "../lib/apollo";
import { SessionProvider } from "next-auth/react";
import "bootstrap/dist/css/bootstrap.min.css";
import Head from "next/head";

export default ({ Component, pageProps }: AppProps) => {
  return (
    <SessionProvider refetchInterval={5 * 60} session={pageProps?.session}>
      <ApolloClientProvider>
        <Head>
          <link rel="shortcut icon" href="/static/assets/icon.png" />
        </Head>
        <Component {...pageProps} />
      </ApolloClientProvider>
    </SessionProvider>
  );
};
