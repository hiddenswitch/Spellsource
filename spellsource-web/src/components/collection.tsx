import {useSession} from "next-auth/react";
import {useState} from "react";
import {CardType} from "../__generated__/spellsource-game";
import {useGetPagedCardsQuery} from "../__generated__/client";
import CardDisplay, {CardDef} from "./card-display";
import styles from "./creative-layout.module.scss";
import { Button } from "react-bootstrap";

const ShowCardTypes: CardType[] = ["MINION", "SPELL", "WEAPON", "HERO"]
const limit = 20;

export default () => {
  const {data: session, status} = useSession();

  const user = session?.token?.sub;

  const [offset, setOffset] = useState(0);
  const [cardType, setCardType] = useState(undefined as CardType | undefined)

  const getCards = useGetPagedCardsQuery({
    variables: {
      limit,
      offset,
      filter: {type: {in: cardType ? [cardType] : ShowCardTypes}}
    }
  });

  const cards = getCards?.data?.allCards?.nodes
    .map(node => node.cardScript as CardDef) ?? []
  const total = getCards?.data?.allCards?.totalCount ?? getCards?.previousData?.allCards?.totalCount ?? 0

  const showing = Math.min(limit, cards.length);

  return (
    <>
      <div id={"Top Bar"} className={styles.collectionTopBar}>
        {(getCards.data || getCards.previousData) && <span>{offset + 1} - {offset + showing} of {total}</span>}
        <span style={{marginLeft: "auto"}}>
          <Button variant={"secondary"} onClick={() => setOffset(Math.max(0, offset - limit))}>Prev</Button>
          <Button variant={"secondary"}  onClick={() => setOffset(Math.min(total - limit, offset + limit))}>Next</Button>
        </span>
      </div>
      <hr/>
      <div id={"Cards"} className={styles.collectionCards}>
        {cards.map((card, i) => (
          <div key={i} className={styles.collectionCard}>
            <CardDisplay {...card} />
          </div>
        ))}
      </div>
    </>
  )
}
