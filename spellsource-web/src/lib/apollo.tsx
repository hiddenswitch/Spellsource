import {
  ApolloClient,
  ApolloProvider,
  createHttpLink,
  InMemoryCache,
  split,
} from "@apollo/client";
import { setContext } from "@apollo/client/link/context";
import { GraphQLWsLink } from "@apollo/client/link/subscriptions";
import { getMainDefinition } from "@apollo/client/utilities";
import { createClient } from "graphql-ws";
import { FunctionComponent, PropsWithChildren, RefObject, useEffect, useRef, useState } from "react";
import { useSession } from "next-auth/react";
import { graphqlHost } from "./config";

function wsUrl(): string {
  return graphqlHost.replace(/^http/, "ws") + "/subscriptions";
}

export const ApolloClientProvider: FunctionComponent<PropsWithChildren> = ({ children }) => {
  const { data: session, status } = useSession();
  const tokenRef = useRef<string | null>(session?.token?.accessToken ?? null);
  const [apolloClient] = useState(() => createApolloClient(tokenRef));

  useEffect(() => {
    tokenRef.current = session?.token?.accessToken ?? null;
  }, [session]);

  return <ApolloProvider client={apolloClient}>{children}</ApolloProvider>;
};

export const createApolloClient = (tokenRef: RefObject<string>) => {
  const httpLink = setContext((_, { headers, ...context }) => ({
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
  );

  const wsLink =
    typeof window !== "undefined"
      ? new GraphQLWsLink(
          createClient({
            url: wsUrl(),
            connectionParams: () => ({
              Authorization: tokenRef.current
                ? `Bearer ${tokenRef.current}`
                : "",
            }),
            shouldRetry: () => true,
            retryAttempts: Infinity,
          })
        )
      : null;

  const link = wsLink
    ? split(
        ({ query }) => {
          const definition = getMainDefinition(query);
          return (
            definition.kind === "OperationDefinition" &&
            definition.operation === "subscription"
          );
        },
        wsLink,
        httpLink
      )
    : httpLink;

  return new ApolloClient({
    link,
    connectToDevTools: process.env.NODE_ENV !== "production",
    cache: new InMemoryCache(),
    defaultOptions: {
      query: {
        fetchPolicy: "cache-first",
        notifyOnNetworkStatusChange: true,
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
