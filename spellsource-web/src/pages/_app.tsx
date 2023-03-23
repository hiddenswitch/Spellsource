import {AppProps} from "next/app";
import "../components/global.scss"
import {useState} from "react";
import {createApolloClient} from "../lib/apollo";
import {ApolloProvider} from "@apollo/client";

export default ({Component, pageProps}: AppProps) => {
  const [apolloClient] = useState(() => createApolloClient());

  return <ApolloProvider client={apolloClient}><Component {...pageProps} /></ApolloProvider>;
};
