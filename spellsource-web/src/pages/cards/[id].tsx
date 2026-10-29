import { useQuery } from "@apollo/client/react";
import { useRouter } from "next/router";
import CardTemplate from "../../templates/card-template";
import { isArray } from "lodash";
import { GetCardDocument } from "../../__generated__/client";

export default () => {
  const router = useRouter();
  const idParam = router.query["id"];
  const id = isArray(idParam) ? idParam.join("_") : idParam ?? "";

  const getCard = useQuery(GetCardDocument, { variables: { id } });
  const cardRecord = getCard.data?.getLatestCard;
  const card = cardRecord?.cardScript ?? undefined;

  return <CardTemplate data={{ card }} />;
};
