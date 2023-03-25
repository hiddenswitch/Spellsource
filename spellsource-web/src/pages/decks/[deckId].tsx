import Layout from '../../components/creative-layout'
import {useRouter} from "next/router";
import {chain, isArray} from "lodash";
import {useGetDeckQuery} from "../../__generated__/client";
import Head from "next/head";
import {CardDef} from "../../components/card-display";
import {useHoverCardDisplay} from "../../components/hover-card-display";
import styles from "../../components/creative-layout.module.scss";
import Collection from "../../components/collection";

export default () => {
  const router = useRouter();

  const idParam = router.query["deckId"];
  const deckId = isArray(idParam) ? idParam.join("/") : idParam;

  const getDeck = useGetDeckQuery({variables: {deckId}});
  const deck = getDeck?.data?.deckById;
  const cards = deck?.cardsInDecksByDeckId?.nodes?.map(node => node.cardByCardId?.cardScript as CardDef) ?? [];

  const groupedCards = chain(cards)
    .groupBy(card => card.id)
    .sortBy(card => card[0].baseManaCost ?? 0)
    .value();

  const {setCard, HoverCardDisplay, ref} = useHoverCardDisplay({translateX: "-50%"});

  return (
    <Layout>
      <Head>
        <title>{deck?.name ?? "Spellsource Decks"}</title>
      </Head>
      <div ref={ref}>
        <HoverCardDisplay/>
        <div id={"Collection"} className={styles.collection}>
          <div id={"CardsList"} className={styles.collectionCardsSection}>
            <Collection/>
          </div>
          <div id={"DecksList"} className={styles.collectionDecksSection}>
            <h3>Cards</h3>
            <ul>
              {groupedCards.map(([card, ...rest]) => (
                <li key={card.id} style={{width: "fit-content"}} onMouseEnter={() => setCard(card)}
                    onMouseLeave={() => setCard(prevState => prevState === card ? null : card)}>
                  {rest.length + 1} x ({card.baseManaCost ?? 0}) {card.name}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </Layout>
  )
}
