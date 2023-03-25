import Layout from '../components/creative-layout'
import {Deck, useGetDecksQuery} from "../__generated__/client";
import {FunctionComponent} from "react";
import styles from "../components/creative-layout.module.scss"
import {useSession} from "next-auth/react";
import Link from 'next/link';
import Collection from "../components/collection";
import Head from "next/head";


const DeckEntry: FunctionComponent<{ deck: Partial<Deck> }> = ({deck}) => (
  <li className={styles.collectionDeck}>
    <Link href={`/decks/${deck.id}`}>{deck.name}</Link>
  </li>
)

export default () => {
  const {data: session, status} = useSession();

  const user = session?.token?.sub;

  const getDecks = useGetDecksQuery({variables: {user}, skip: !user})

  const allDecks = [
    ...(getDecks.data?.allDecks?.nodes ?? []),
    ...(getDecks.data?.allDeckShares?.nodes?.map(node => node.deckByDeckId) ?? [])
  ]

  return (
    <Layout>
      <Head>
        <title>Spellsource Cards</title>
      </Head>
      <div id={"Collection"} className={styles.collection}>
        <div id={"CardsList"} className={styles.collectionCardsSection}>
          <Collection/>
        </div>
        <div id={"DecksList"} className={styles.collectionDecksSection}>
          <h3>Decks</h3>
          <ul>
            {allDecks.filter(deck => !deck.isPremade).map(deck => (
              <DeckEntry key={deck.id}  deck={deck}/>
            ))}
            <hr/>
            {allDecks.filter(deck => deck.isPremade).map(deck => (
              <DeckEntry key={deck.id}  deck={deck}/>
            ))}
          </ul>
        </div>
      </div>
    </Layout>
  )
}
