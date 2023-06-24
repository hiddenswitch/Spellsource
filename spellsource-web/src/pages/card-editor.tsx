import React, { createContext, useMemo } from "react"
import Loadable from "react-loadable"
import Layout from "../components/card-editor-layout"
import * as BlocklyMiscUtils from "../lib/blockly-misc-utils"
import * as styles from "../templates/template-styles.module.scss"
import { GetStaticPropsContext, InferGetStaticPropsType } from "next"
import path from "path"
import { BlockDef } from "../__generated__/blocks"
import { readAllImages, readAllJson } from "../lib/fs-utils"
import { CardDef } from "../components/card-display"
import { fixArt, transformBlock } from "../lib/json-transforms"
import { keyBy } from "lodash"
import {
  Card,
  CollectionCard,
  GetCardsQuery,
  ImageDef,
  useGetAllArtQuery,
  useGetCardsQuery,
  useGetClassesQuery,
} from "../__generated__/client"
import { useSession } from "next-auth/react"
import { ApolloQueryResult } from "@apollo/client"
import Head from "next/head"
import CardEditorView from "../components/card-editor-view"
import { Spinner } from "react-bootstrap"

const getAllBlockJson = async () =>
  (await readAllJson<BlockDef[]>(path.join("src", "blocks", "*.json"))).flat(1).map(transformBlock)

const getAllArt = async () => readAllImages(path.join("card-images", "art", "**", "*.png"))

const getAllIcons = async () => readAllImages(path.join("assets", "editor", "*.png"))

/*const getAllCards = async () => (await readAllJson<CardDef>(
  path.join("..", "spellsource-game", "src", "main", "resources", "basecards", "standard", "**", "*.json"),
  (json, file) => json.id ||= path.basename(file, ".json")
)).map(transformCard)*/

export const getStaticProps = async (context: GetStaticPropsContext) => {
  const allBlocks = await getAllBlockJson()
  const blocksByType = keyBy(allBlocks, (block) => block.type)
  // const allArt = await getAllArt();
  const allIcons = await getAllIcons()

  return {
    props: { allBlocks, allIcons, blocksByType },
  }
}

export const BlocklyDataContext = createContext({ ready: false } as InferGetStaticPropsType<typeof getStaticProps> & {
  ready: boolean
  classes: Record<string, CardDef>
  allArt: ImageDef[]
  myCards: Partial<CollectionCard>[]
  refreshMyCards: () => Promise<ApolloQueryResult<GetCardsQuery>>
  userId: string | null | undefined
})

const CardEditor = (props: InferGetStaticPropsType<typeof getStaticProps>) => {
  const { data: session } = useSession()
  const userId = session?.token?.sub ?? ""

  const getClasses = useGetClassesQuery({
    variables: {
      filter: {
        createdBy: { notEqualToInsensitive: userId },
        collectible: { equalTo: true },
      },
    },
  })
  const getMyCards = useGetCardsQuery({
    variables: {
      filter: {
        createdBy: { equalToInsensitive: userId },
      },
    },
  })
  const classes = useMemo(() => {
    const cards = getClasses.data?.allClasses?.nodes ?? []
    const allCards = cards.map((card) => ({
      ...(card.cardScript ?? {}),
      id: card.id,
    }))
    const cardsById = keyBy(allCards, (card) => card.id)
    fixArt(cardsById)

    return cardsById
  }, [getClasses.data])

  const getAllArt = useGetAllArtQuery()
  const allArt = getAllArt.data?.allArt ?? []

  const ready = Object.values(classes).length > 0 && allArt.length > 0 && typeof window !== "undefined"

  const myCards = useMemo(
    () => (getMyCards.data?.allCollectionCards?.nodes ?? []).filter((card) => card.blocklyWorkspace),
    [getMyCards.data]
  )

  const refreshMyCards = getMyCards.refetch // TODO get .reobserve working

  return (
    <Layout>
      <Head>
        <title>Spellsource Card Editor</title>
      </Head>
      <BlocklyDataContext.Provider value={{ ...props, classes, allArt, ready, myCards, refreshMyCards, userId }}>
        <div className={styles.cardEditorContainer}>
          {ready ? (
            <CardEditorView defaultCard={false} />
          ) : (
            <div className={"h-100 w-100 d-flex"}>
              <Spinner variant={"placeholder"} className={"m-auto"} />
            </div>
          )}
        </div>
      </BlocklyDataContext.Provider>
    </Layout>
  )
}

export default CardEditor
