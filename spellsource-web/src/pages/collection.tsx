import Layout from "../components/creative-layout"
import {
  DeckFragment,
  useCreateDeckMutation,
  useGetClassesQuery,
  useGetDeckQuery,
  useGetDecksQuery,
} from "../__generated__/client"
import React, { createContext, FunctionComponent, useEffect, useMemo, useRef, useState } from "react"
import { useSession } from "next-auth/react"
import Collection, { textDecorationStyle } from "../components/collection"
import Head from "next/head"
import { Button, Col, Dropdown, Offcanvas, OffcanvasBody, OffcanvasHeader, Row } from "react-bootstrap"
import { chain, keyBy } from "lodash"
import { useRouter } from "next/router"
import { CardDef, toRgbaString } from "../components/card-display"
import Link from "next/link"
import { useParam, useParamInt } from "../lib/routing"
import DropdownToggle from "react-bootstrap/DropdownToggle"
import DropdownMenu from "react-bootstrap/DropdownMenu"
import DropdownItem from "react-bootstrap/DropdownItem"
import { useList } from "react-use"
import { DndProvider } from "react-dnd"
import { HTML5Backend } from "react-dnd-html5-backend"
import { Deck } from "../components/deck"
import cx from "classnames"

// Makes the query state stick around
export const getServerSideProps = async (context) => ({
  props: {},
})

export const CardCache = createContext<Record<string, CardDef>>({})

export default () => {
  const { data: session } = useSession()
  const user = session?.token?.sub

  const cacheRef = useRef<Record<string, CardDef>>({})
  const cache = cacheRef.current

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
      <Link draggable={false} href={{ query: { ...router.query, deckId: deck.id, heroClass: "ALLOWED" } }}>
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

  const getDeck = useGetDeckQuery({
    variables: { deckId },
    onCompleted: (data) =>
      data.deckById?.cardsInDecksByDeckId?.nodes?.forEach(
        (node) => (cache[node.cardId] = node.cardByCardId.cardScript)
      ),
  })
  const deck = getDeck?.data?.deckById ?? getDeck?.previousData?.deckById

  const realCards = useMemo(() => deck?.cardsInDecksByDeckId?.nodes?.map((node) => node.cardId) ?? [], [deck])

  const [deckCards, deckActions] = useList([] as string[])

  const { set: setCards, clear: clearCards, removeAt: removeCard, push: addCardToDeck } = deckActions
  const removeCardFromDeck = (id: string) => removeCard(deckCards.findIndex((card) => card === id))

  useEffect(() => {
    setCards([...realCards])
  }, [realCards])

  const myDeck = deck && user && deck.createdBy === user

  const [createDeck] = useCreateDeckMutation()

  const createNewDeck = async (heroClass: string, className: string) => {
    const { data } = await createDeck({
      variables: {
        heroClass,
        deckName: `New ${className} Deck`,
        format: "Spellsource",
        cardIds: [],
      },
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
  }

  const [show, setShow] = useState(false)

  return (
    <Layout>
      <Head>
        <title>Spellsource Cards</title>
      </Head>
      <DndProvider backend={HTML5Backend}>
        <CardCache.Provider value={cache}>
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
                removeFromDeck={myDeck ? removeCardFromDeck : undefined}
              />
            </Col>
            <Col id={"DecksList"} xs={12} lg={3}>
              <Offcanvas
                responsive={"lg"}
                show={show}
                onHide={() => setShow(false)}
                placement={"bottom"}
                scroll={true}
                backdrop={false}
              >
                <OffcanvasBody>
                  {selectedDeck ? (
                    <Deck
                      user={user}
                      deck={deck}
                      myDeck={myDeck}
                      cardIds={deckCards}
                      cardActions={deckActions}
                      getDecks={getDecks}
                      getDeck={getDeck}
                      classColors={classColors}
                      setDeckId={setDeckId}
                      realCards={realCards}
                    />
                  ) : (
                    <div className={"w-100"}>
                      {user && (
                        <>
                          <h3 className={"d-flex flex-row gap-3 align-items-baseline mb-0"}>
                            Your Decks
                            <Dropdown className={"ms-auto"}>
                              <DropdownToggle>Create</DropdownToggle>
                              <DropdownMenu>
                                {Object.entries(classes).map(([heroClass, className]) => (
                                  <DropdownItem
                                    onClick={async () => await createNewDeck(heroClass, className)}
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
                    </div>
                  )}
                  <Button
                    className={"position-absolute w-auto top-0 end-0 me-2"}
                    style={{ transform: "translate(0%, -110%)" }}
                    onClick={() => setShow(!show)}
                  >
                    {show ? "Close" : deckId ? "Deck" : "Decks"}
                  </Button>
                </OffcanvasBody>
              </Offcanvas>
            </Col>
            <div className={"fixed-bottom d-flex p-0"}>
              <Button className={"d-lg-none ms-auto m-2"} onClick={() => setShow(!show)}>
                {deckId ? "Deck" : "Decks"}
              </Button>
            </div>
          </Row>
        </CardCache.Provider>
      </DndProvider>
    </Layout>
  )
}
