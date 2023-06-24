import { ApolloClient, ApolloProvider, createHttpLink, InMemoryCache } from "@apollo/client"
import { FunctionComponent, PropsWithChildren, useEffect, useState } from "react"
import { useSession } from "next-auth/react"
import { usePrevious } from "react-use"

export const ApolloClientProvider: FunctionComponent<PropsWithChildren> = ({ children }) => {
  const [apolloClient] = useState(() => createApolloClient())

  const { status } = useSession()
  const prevStatus = usePrevious(status)

  useEffect(() => {
    if (prevStatus === "authenticated" && status === "unauthenticated") {
      console.log("Clearing the cache")
      apolloClient.clearStore()
    }
  }, [status])

  return <ApolloProvider client={apolloClient}>{children}</ApolloProvider>
}

export const createApolloClient = () =>
  new ApolloClient({
    link: createHttpLink({
      uri: (typeof window !== "undefined" ? window.location.origin : "http://localhost:3000") + "/api/graphql",
      fetch,
    }),
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
  })
