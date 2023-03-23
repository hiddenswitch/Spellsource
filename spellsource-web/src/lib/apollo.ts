import {ApolloClient, createHttpLink, InMemoryCache} from "@apollo/client";


export const createApolloClient = () => {

  const httpLink = createHttpLink({uri: `http://localhost:3000/api/graphql`, fetch})

  return new ApolloClient({
    link: httpLink,
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
}
