import Layout from "../components/creative-layout"
import {Deck, useGetDeckQuery, useGetDecksQuery} from "../__generated__/client"
import React, {FunctionComponent, useMemo} from "react"
import {useSession} from "next-auth/react"
import Collection from "../components/collection"
import Head from "next/head"
import {Button, Col, Row} from "react-bootstrap"
import {chain, isArray, keyBy} from "lodash"
import {useRouter} from "next/router"
import {CardDef} from "../components/card-display"
import Link from "next/link"

export default () => {
  const {data: session} = useSession()
  const user = session?.token?.sub

  const router = useRouter()
  const deckIdParam = router.query.deckId
  const deckId = isArray(deckIdParam) ? deckIdParam.join("/") : deckIdParam

  const getDecks = useGetDecksQuery({variables: {user}, skip: !user})
  const allDecks = [
    ...(getDecks.data?.allDecks?.nodes ?? []),
    ...(getDecks.data?.allDeckShares?.nodes?.map((node) => node.deckByDeckId) ?? []),
  ]
  const decksById = useMemo(() => keyBy(allDecks, (value) => value.id), [allDecks])
  const selectedDeck = decksById[deckId]

  const DeckEntry: FunctionComponent<{ deck: Partial<Deck> }> = ({deck}) => (
    <li className={""}>
      <Link href={`/collection?deckId=${deck.id}`}>{deck.name}</Link>
    </li>
  )

  const getDeck = useGetDeckQuery({variables: {deckId}})
  const deck = getDeck?.data?.deckById
  const cards = deck?.cardsInDecksByDeckId?.nodes?.map((node) => node.cardByCardId?.cardScript as CardDef) ?? []

  const groupedCards = chain(cards)
    .groupBy((card) => card.id)
    .sortBy((card) => card[0].baseManaCost ?? 0)
    .value()

  return (
    <Layout>
      <Head>
        <title>Spellsource Cards</title>
      </Head>
      <Row id={"Collection"}>
        <Col id={"CardsList"} xs={12} lg={9}>
          <Collection/>
        </Col>
        <Col id={"DecksList"} xs={3} className={"d-none d-lg-block"}>
          <h3>{selectedDeck && deck ? deck.name : "Decks"}</h3>
          <ul className={"d-flex flex-column gap-2"}>
            {selectedDeck && deck ? (
              groupedCards.map(([card, ...rest]) => (
                <li key={card.id} style={{width: "fit-content"}}>
                  {rest.length + 1} x ({card.baseManaCost ?? 0}) {card.name}
                </li>
              ))
            ) : (
              <>
                {allDecks
                  .filter((deck) => !deck.isPremade)
                  .map((deck) => (
                    <DeckEntry key={deck.id} deck={deck}/>
                  ))}
                {!allDecks.filter((deck) => !deck.isPremade).length || <hr/>}
                {allDecks
                  .filter((deck) => deck.isPremade)
                  .map((deck) => (
                    <DeckEntry key={deck.id} deck={deck}/>
                  ))}
              </>
            )}
          </ul>
          {selectedDeck && deck && (
            <Link href={"/collection"}>
              <Button variant={"secondary"}>Back</Button>
            </Link>
          )}
        </Col>
      </Row>
    </Layout>
  )
}
