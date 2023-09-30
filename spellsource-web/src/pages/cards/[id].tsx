import { useRouter } from "next/router";
import CardTemplate from "../../templates/card-template";
import { isArray } from "lodash";
import { useGetCardQuery } from "../../__generated__/client";

export default () => {
  const router = useRouter();
  const idParam = router.query["id"];
  const id = isArray(idParam) ? idParam.join("_") : idParam ?? "";

  const getCard = useGetCardQuery({ variables: { id } });
  const cardRecord = getCard.data?.getLatestCard;
  const card = cardRecord?.cardScript ?? undefined;

  return <CardTemplate data={{ card }} />;
};
