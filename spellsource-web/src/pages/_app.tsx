import {AppProps} from "next/app";
import "../components/global.scss"
import {useState} from "react";
import {createApolloClient} from "../lib/apollo";
import {ApolloProvider} from "@apollo/client";
import {SessionProvider} from "next-auth/react";

export default ({Component, pageProps}: AppProps) => {
  const [apolloClient] = useState(() => createApolloClient());

  return (
    <SessionProvider refetchInterval={5 * 60}>
      <ApolloProvider client={apolloClient}>
        <Component {...pageProps} />
      </ApolloProvider>
    </SessionProvider>
  );
};
