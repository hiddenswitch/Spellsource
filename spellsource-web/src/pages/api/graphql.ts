import {startServerAndCreateNextHandler} from '@as-integrations/next';
import {createApolloServer} from "../../server/apollo-server";

/*
export const config = {
  api: {
    bodyParser: false,
  },
}
*/


export default startServerAndCreateNextHandler(await createApolloServer());
