import React, {createContext} from 'react'
import Loadable from 'react-loadable'
import Layout from '../components/card-editor-layout'
import BlocklyMiscUtils from "../lib/blockly-misc-utils";
import * as styles from '../templates/template-styles.module.scss';
import {GetStaticPropsContext, InferGetStaticPropsType} from "next";
import path from "path";
import {BlockDef} from "../lib/blocks";
import {readAllImages, readAllJson} from "../lib/fs-utils";
import {CardDef} from "../components/card-display";
import {transformBlock, transformCard} from "../lib/json-transforms";
import {keyBy} from "lodash";

const getAllBlockJson = async () =>
  (await readAllJson<BlockDef[]>(path.join("src", "blocks", "*.json")))
    .flat(1)
    .map(transformBlock)

const getAllArt = async () => readAllImages(path.join("card-images", "art", "**", "*.png"))

const getAllIcons = async () => readAllImages(path.join("assets", "editor", "*.png"))

const getAllCards = async () => [
  ...await readAllJson<CardDef>(
    path.join("..", "spellsource-cards-git", "src", "main", "resources", "cards", "**", "*.json"),
    (json, file) => json.id ||= path.basename(file, ".json")
  ),
  ...await readAllJson<CardDef>(
    path.join("..", "spellsource-game", "src", "main", "resources", "basecards", "standard", "**", "*.json"),
    (json, file) => json.id ||= path.basename(file, ".json")
  )
].map(transformCard)

export const getStaticProps = async (context: GetStaticPropsContext) => {
  const allBlocks = await getAllBlockJson();
  const blocksByType = keyBy(allBlocks, block => block.type);
  const allArt = await getAllArt();
  const allIcons = await getAllIcons();
  const allCards = await getAllCards();

  return {
    props: {allBlocks, allArt, allIcons, allCards, blocksByType}
  }
}

export const BlocklyDataContext = createContext({ready: false} as InferGetStaticPropsType<typeof getStaticProps> & { ready: boolean })

const LoadableComponent = Loadable.Map({
  loader: {
    Blockly: () => import('blockly'),
    CardEditorView: () => import('../components/card-editor-view')
  },
  delay: 300,
  loading() {
    return (<span>Loading</span>)
  },
  render(loaded) {
    const Blockly = loaded.Blockly
    const CardEditorView = loaded.CardEditorView.default

    BlocklyMiscUtils.loadableInit(Blockly)

    return <CardEditorView defaultCard={false}/>
  }
})

const CardEditor = (props: InferGetStaticPropsType<typeof getStaticProps>) => {

  return <Layout>
    <BlocklyDataContext.Provider value={{...props, ready: true}}>
      <div className={styles.cardEditorContainer}>
        <LoadableComponent/>
      </div>
    </BlocklyDataContext.Provider>
  </Layout>
}

export default CardEditor
