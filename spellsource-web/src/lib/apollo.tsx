import { ApolloProvider } from "@apollo/client/react";
import { ApolloClient, createHttpLink, InMemoryCache } from "@apollo/client";
import { setContext } from "@apollo/client/link/context";
import { FunctionComponent, PropsWithChildren, RefObject, useEffect, useRef, useState } from "react";
import { useSession } from "next-auth/react";
import { graphqlHost } from "./config";
import { useUpdateEffect } from "react-use";

export const ApolloClientProvider: FunctionComponent<PropsWithChildren> = ({ children }) => {
  const { data: session, status } = useSession();
  const tokenRef = useRef<string | null>(session?.token?.accessToken ?? null);
  const [apolloClient] = useState(() => createApolloClient(tokenRef));

  useEffect(() => {
    tokenRef.current = session?.token?.accessToken ?? null;
  }, [session]);

  return <ApolloProvider client={apolloClient}>{children}</ApolloProvider>;
};

export const createApolloClient = (tokenRef: RefObject<string | null>) => {
  return new ApolloClient({
    link: setContext((_, { headers, ...context }) => ({
      headers: {
        ...headers,
        Authorization: tokenRef.current ? `Bearer ${tokenRef.current}` : "",
      },
      ...context,
    })).concat(
      createHttpLink({
        uri: graphqlHost + "/graphql",
        fetch,
      })
    ),
    devtools: { enabled: process.env.NODE_ENV !== "production" },
    cache: new InMemoryCache(),
    defaultOptions: {
      query: {
        fetchPolicy: "cache-first",
      },
      watchQuery: {
        fetchPolicy: "cache-and-network",
        nextFetchPolicy: "cache-first",
        notifyOnNetworkStatusChange: true,
      },
      mutate: {
        fetchPolicy: "network-only",
      },
    },
  });
};
