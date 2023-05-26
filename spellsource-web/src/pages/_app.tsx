import {AppProps} from "next/app";
import "../components/global.scss"
import {ApolloClientProvider} from "../lib/apollo";
import {SessionProvider} from "next-auth/react";
import 'bootstrap/dist/css/bootstrap.min.css';
import Head from "next/head";

export default ({Component, pageProps}: AppProps) => {
  return (
    <SessionProvider refetchInterval={5 * 60}>
      <ApolloClientProvider>
        <Head>
          <link rel="shortcut icon" href="/static/assets/icon.png" />
        </Head>
        <Component {...pageProps} />
      </ApolloClientProvider>
    </SessionProvider>
  );
};
