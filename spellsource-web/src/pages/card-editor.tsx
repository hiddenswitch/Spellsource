import React, {createContext, useMemo} from 'react'
import Loadable from 'react-loadable'
import Layout from '../components/card-editor-layout'
import * as BlocklyMiscUtils from "../lib/blockly-misc-utils";
import * as styles from '../templates/template-styles.module.scss';
import {GetStaticPropsContext, InferGetStaticPropsType} from "next";
import path from "path";
import {BlockDef} from "../lib/blocks";
import {readAllImages, readAllJson} from "../lib/fs-utils";
import {CardDef} from "../components/card-display";
import {fixArt, transformBlock} from "../lib/json-transforms";
import {keyBy} from "lodash";
import {Card, ImageDef, useGetAllArtQuery, useGetCardsQuery} from "../__generated__/client";
import {useSession} from "next-auth/react";

const getAllBlockJson = async () =>
  (await readAllJson<BlockDef[]>(path.join("src", "blocks", "*.json")))
    .flat(1)
    .map(transformBlock)

const getAllArt = async () => readAllImages(path.join("card-images", "art", "**", "*.png"))

const getAllIcons = async () => readAllImages(path.join("assets", "editor", "*.png"))

/*const getAllCards = async () => (await readAllJson<CardDef>(
  path.join("..", "spellsource-game", "src", "main", "resources", "basecards", "standard", "**", "*.json"),
  (json, file) => json.id ||= path.basename(file, ".json")
)).map(transformCard)*/

export const getStaticProps = async (context: GetStaticPropsContext) => {
  const allBlocks = await getAllBlockJson();
  const blocksByType = keyBy(allBlocks, block => block.type);
  // const allArt = await getAllArt();
  const allIcons = await getAllIcons();

  return {
    props: {allBlocks, allIcons, blocksByType}
  }
}

export const BlocklyDataContext = createContext(
  {ready: false} as InferGetStaticPropsType<typeof getStaticProps> & {
    ready: boolean
    cardsById: Record<string, CardDef>
    allArt: ImageDef[]
    myCards: Partial<Card>[]
  }
)

const LoadableComponent = Loadable.Map({
  loader: {
    Blockly: () => import('blockly'),
    CardEditorView: () => import('../components/card-editor-view')
  },
  delay: 300,
  loading() {
    return (<span>Loading</span>)
  },
  render(loaded, props: { dataReady: boolean }) {
    if (!props.dataReady)
      return (<span>Loading</span>)

    const Blockly = loaded.Blockly
    const CardEditorView = loaded.CardEditorView.default

    BlocklyMiscUtils.loadableInit(Blockly)

    return <CardEditorView defaultCard={false}/>
  }
})

const CardEditor = (props: InferGetStaticPropsType<typeof getStaticProps>) => {
  const {data: session} = useSession();
  const userId = session?.token?.sub ?? "";

  const getCards = useGetCardsQuery({variables: {filter: {or: [{type: {equalTo: "CLASS"}}, {createdBy: {equalTo: userId}}]}}});
  const cardsById = useMemo(() => {
    const cards = getCards.data?.allCards?.nodes ?? [];
    const allCards = cards.map(card => ({
      ...(card.cardScript ?? {}),
      id: card.id,
    }));
    const cardsById = keyBy(allCards, card => card.id);
    fixArt(cardsById);

    return cardsById
  }, [getCards.data]);

  const getAllArt = useGetAllArtQuery();
  const allArt = getAllArt.data?.allArt ?? [];

  const ready = Object.values(cardsById).length > 0 && allArt.length > 0;

  const myCards = useMemo(
    () => (getCards.data?.allCards?.nodes ?? []).filter(card => card.createdBy === userId && card.blocklyWorkspace),
    [getCards.data])

  return <Layout>
    <BlocklyDataContext.Provider value={{...props, cardsById, allArt, ready, myCards}}>
      <div className={styles.cardEditorContainer}>
        <LoadableComponent dataReady={ready}/>
      </div>
    </BlocklyDataContext.Provider>
  </Layout>
}

export default CardEditor
