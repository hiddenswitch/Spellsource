import Layout from "../components/creative-layout"
import {
  DeckFragment,
  useCreateDeckMutation,
  useDeleteDeckMutation,
  useGetClassesQuery,
  useGetDeckQuery,
  useGetDecksQuery,
  useRenameDeckMutation,
  useSetCardsInDeckMutation,
} from "../__generated__/client"
import React, { FunctionComponent, useEffect, useMemo } from "react"
import { useSession } from "next-auth/react"
import Collection, {textDecorationStyle} from "../components/collection"
import Head from "next/head"
import { Button, Col, Dropdown, Row } from "react-bootstrap"
import { chain, isEqual, keyBy } from "lodash"
import { useRouter } from "next/router"
import { CardDef, toRgbaString } from "../components/card-display"
import Link from "next/link"
import { useParam, useParamInt } from "../lib/routing"
import DropdownToggle from "react-bootstrap/DropdownToggle"
import DropdownMenu from "react-bootstrap/DropdownMenu"
import DropdownItem from "react-bootstrap/DropdownItem"
import { useList } from "react-use"

// Makes the query state stick around
export const getServerSideProps = async (context) => ({
  props: {},
})

export default () => {
  const { data: session } = useSession()
  const user = session?.token?.sub

  const router = useRouter()
  const [offset, setOffset] = useParamInt(router, "offset")
  const [deckId, setDeckId] = useParam(router, "deckId")
  const [heroClass, setHeroClass] = useParam(router, "heroClass")

  const getClasses = useGetClassesQuery({ variables: { filter: { collectible: { equalTo: true } } } })
  const classes = useMemo(
    () =>
      chain(getClasses.data?.allClasses?.nodes ?? [])
        .filter((card) => card.class !== "ANY")
        .keyBy((card) => card.class)
        .mapValues((card) => (card.cardScript as CardDef).name)
        .value(),
    [getClasses.data]
  )
  const classColors = useMemo(
    () =>
      chain(getClasses.data?.allClasses?.nodes ?? [])
        .filter((card) => card.class !== "ANY")
        .keyBy((card) => card.class)
        .mapValues((card) => toRgbaString((card.cardScript as CardDef).art?.primary))
        .value(),
    [getClasses.data]
  )
  const textColors = useMemo(
    () =>
      chain(getClasses.data?.allClasses?.nodes ?? [])
        .filter((card) => card.class !== "ANY")
        .keyBy((card) => card.class)
        .mapValues((card) => toRgbaString((card.cardScript as CardDef).art?.body?.vertex))
        .value(),
    [getClasses.data]
  )

  const getDecks = useGetDecksQuery({ variables: { user } })
  const allDecks = [
    ...(getDecks.data?.allDecks?.nodes ?? []),
    ...(getDecks.data?.allDeckShares?.nodes?.map((node) => node.deckByDeckId) ?? []),
  ]
  const decksById = useMemo(() => keyBy(allDecks, (value) => value.id), [allDecks])
  const selectedDeck = decksById[deckId]

  const DeckEntry: FunctionComponent<{ deck: DeckFragment }> = ({ deck }) => (
    <li className={"d-flex flex-row align-items-baseline gap-2"}>
      <Link href={{ query: { ...router.query, deckId: deck.id, heroClass: "ALLOWED" } }}>
        <Button
          variant={"light"}
          className={""}
          style={{ borderColor: deck.heroClass in classColors ? classColors[deck.heroClass] : "initial" }}
        >
          {deck.name}
        </Button>
      </Link>
    </li>
  )

  const getDeck = useGetDeckQuery({ variables: { deckId } })
  const deck = getDeck?.data?.deckById ?? getDeck?.previousData?.deckById
  const myDeck = deck && user && deck.createdBy === user
  const realCards = useMemo(
    () => deck?.cardsInDecksByDeckId?.nodes?.map((node) => node.cardByCardId?.cardScript as CardDef) ?? [],
    [deck]
  )

  const [cards, { set: setCards, clear: clearCards, removeAt: removeCard, push: addCardToDeck }] = useList(
    [] as CardDef[]
  )

  useEffect(() => {
    setCards([...realCards])
  }, [realCards])

  const groupedCards = chain(cards)
    .groupBy((card) => card.id)
    .sortBy((card) => card[0].baseManaCost ?? 0)
    .value()

  const [createDeck] = useCreateDeckMutation()
  const [setCardsInDeck] = useSetCardsInDeckMutation()
  const [deleteDeck] = useDeleteDeckMutation()
  const [renameDeck] = useRenameDeckMutation()

  return (
    <Layout>
      <Head>
        <title>Spellsource Cards</title>
      </Head>
      <Row id={"Collection"} className={"pe-lg-2"}>
        <Col id={"CardsList"} xs={12} lg={9}>
          <Collection
            classes={classes}
            classColors={classColors}
            heroClass={heroClass}
            setHeroClass={setHeroClass}
            offset={offset}
            setOffset={setOffset}
            mainHeroClass={deck?.heroClass}
            addToDeck={myDeck ? addCardToDeck : undefined}
          />
        </Col>
        <Col id={"DecksList"} xs={3} className={"d-none d-lg-block"}>
          {selectedDeck ? (
            <>
              <h3 className={"d-flex flex-row gap-2 align-items-baseline flex-wrap"}>
                <span style={textDecorationStyle(deck?.heroClass, classColors)}>{deck?.name ?? "Your Decks"}</span>
                <span className={`ms-auto ${cards.length > 30 && "text-danger"}`}>{cards.length}/30</span>
              </h3>
              <ul className={"d-flex flex-column gap-2"}>
                {groupedCards.map(([card, ...rest]) => (
                  <li key={card.id} className={"d-flex flex-row align-items-baseline"}>
                    <span className={`${rest.length + 1 > (card.rarity === "LEGENDARY" ? 1 : 2) && "text-danger"}`}>
                      {rest.length + 1} x
                    </span>
                    <Button
                      variant={"light"}
                      className={`ms-2 ${(card.heroClass !== "ANY" && card.heroClass !== deck?.heroClass) && "text-danger"}`}
                      style={{ pointerEvents: myDeck ? "initial" : "none", ...textDecorationStyle(card.heroClass, classColors) }}
                      onClick={() => {
                        const index = cards.findIndex((value) => value.id == card.id)
                        if (index >= 0) {
                          removeCard(index)
                        }
                      }}
                    >
                      ({card.baseManaCost ?? 0}) {card.name}
                    </Button>
                  </li>
                ))}
              </ul>
              <div className={"d-flex flex-row gap-3 flex-wrap"}>
                <Button
                  variant={"secondary"}
                  onClick={async () => {
                    if (isEqual(cards, realCards) || confirm("Leave and discard changes?")) {
                      await router.push({ query: { ...router.query, deckId: undefined, heroClass: undefined } })
                    }
                  }}
                >
                  Back
                </Button>
                {user && (
                  <Button
                    onClick={async () => {
                      const { data } = await createDeck({
                        variables: {
                          heroClass: deck.heroClass,
                          deckName: `Duplicate of ${deck.name}`,
                          cardIds: deck.cardsInDecksByDeckId.nodes.map((value) => value.cardId),
                          format: deck.format
                        },
                      })
                      const newDeckId = data?.createDeckWithCards?.deck?.id
                      if (newDeckId) {
                        await getDecks.refetch()
                        await setDeckId(newDeckId)
                      }
                    }}
                  >
                    Duplicate
                  </Button>
                )}
                {myDeck && (
                  <>
                    <Button
                      variant={"light"}
                      onClick={async () => {
                        let newName = prompt("New Deck Name")
                        if (!newName) return

                        const { data } = await renameDeck({ variables: { deckId: deck.id, deckName: newName } })
                        if (data.updateDeckById?.deck?.name) {
                          await getDecks.refetch()
                        }
                      }}
                    >
                      Rename
                    </Button>
                    <Button
                      variant={"danger"}
                      onClick={async () => {
                        if (!confirm(`Are you sure you want to delete "${deck.name}"?`)) return

                        const { data } = await deleteDeck({ variables: { deckId: deck.id } })
                        if (data.updateDeckById?.deck?.trashed) {
                          await getDecks.refetch()
                          await router.replace({ query: { ...router.query, deckId: undefined, heroClass: undefined } })
                        }
                      }}
                    >
                      Delete
                    </Button>
                    <Button
                      disabled={isEqual(cards, realCards) || getDeck.loading}
                      variant={"light"}
                      onClick={() => setCards(realCards)}
                    >
                      Reset
                    </Button>
                    <Button
                      disabled={isEqual(cards, realCards) || getDeck.loading}
                      onClick={async () => {
                        const { data } = await setCardsInDeck({
                          variables: {
                            deckId: deck.id,
                            cardIds: cards.map((card) => card.id),
                          },
                        })
                        if (data?.setCardsInDeck?.cardsInDecks) {
                          await getDeck.refetch()
                        }
                      }}
                    >
                      Save
                    </Button>
                  </>
                )}
              </div>
            </>
          ) : (
            <>
              {user && (
                <>
                  <h3 className={"d-flex flex-row gap-3 align-items-baseline mb-0"}>
                    Your Decks
                    <Dropdown className={"ms-auto"}>
                      <DropdownToggle>Create</DropdownToggle>
                      <DropdownMenu>
                        {Object.entries(classes).map(([heroClass, className]) => (
                          <DropdownItem
                            onSelect={async () => {
                              const { data } = await createDeck({
                                variables: { heroClass, deckName: `New ${className} Deck`, format: "Spellsource", cardIds: [] },
                              })

                              const newDeckId = data?.createDeckWithCards?.deck?.id
                              if (newDeckId) {
                                await getDecks.refetch()
                                await router.replace({
                                  query: {
                                    ...router.query,
                                    deckId: newDeckId,
                                    heroClass: "ALLOWED",
                                  },
                                })
                              }
                            }}
                            style={textDecorationStyle(heroClass, classColors)}
                          >
                            {className}
                          </DropdownItem>
                        ))}
                      </DropdownMenu>
                    </Dropdown>
                  </h3>
                  <ul className={"d-flex flex-column gap-3 mb-1"}>
                    {allDecks
                      .filter((deck) => !deck.isPremade)
                      .map((deck) => (
                        <DeckEntry key={deck.id} deck={deck} />
                      ))}
                  </ul>
                </>
              )}
              <h3>Premade Decks</h3>
              <ul className={"d-flex flex-column gap-3"}>
                {allDecks
                  .filter((deck) => deck.isPremade)
                  .map((deck) => (
                    <DeckEntry key={deck.id} deck={deck} />
                  ))}
              </ul>
            </>
          )}
        </Col>
      </Row>
    </Layout>
  )
}
