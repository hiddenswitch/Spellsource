import {startServerAndCreateNextHandler} from '@as-integrations/next';
import {createApolloServer} from "../../server/apollo-server";
import {getSession} from "next-auth/react";

export default startServerAndCreateNextHandler(await createApolloServer(), {
  context: async req => {
    const session = await getSession({req})
    return {session}
  }
});
