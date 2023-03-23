import {useRouter} from "next/router";
import CardTemplate from "../../templates/card-template";
import {isArray} from "lodash";
import {GetCardDocument, GetCardQuery, GetCardQueryVariables, useGetCardQuery} from "../../__generated__/client";
import {GetServerSidePropsContext} from "next";
import {createApolloClient} from "../../lib/apollo";

export default () => {
  const router = useRouter();
  const idParam = router.query["id"];
  const id = isArray(idParam) ? idParam.join("_") : idParam;

  const getCard = useGetCardQuery({variables: {id}});
  const cardRecord = getCard.data?.cardById;
  const card = cardRecord ? JSON.parse(cardRecord.cardScript) : undefined;

  return <CardTemplate data={{card}}/>
}
