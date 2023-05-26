import {startServerAndCreateNextHandler} from '@as-integrations/next';
import {createApolloServer} from "../../server/apollo-server";
import {getSessionDirect} from "./auth/[...nextauth]";

export default startServerAndCreateNextHandler(await createApolloServer(), {
  context: async req => {
    const session = await getSessionDirect(req);
    return {session}
  }
});
