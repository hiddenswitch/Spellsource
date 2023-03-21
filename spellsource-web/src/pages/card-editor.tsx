import React, {createContext} from 'react'
import Loadable from 'react-loadable'
import Layout from '../components/card-editor-layout'
import BlocklyMiscUtils from "../lib/blockly-misc-utils";
import * as styles from '../templates/template-styles.module.scss';
import {GetStaticPropsContext, InferGetStaticPropsType} from "next";
import * as glob from "glob-promise";
import path from "path";
import probe from "probe-image-size";
import fs from "fs";

const getAllBlockJson = async () => {
  const blockFiles = await glob.promise(path.join(process.cwd(), "src", "blocks", "*.json"));
  return await Promise.all(blockFiles.map(file => fs.promises.readFile(file, {encoding: "utf8"})));
}

const getAllArt = async () => {
  const staticPath = path.join(process.cwd(), "public", "static");
  const artFiles = await glob.promise(path.join(staticPath, "card-images", "art", "**", "*.png"));

  return await Promise.all(artFiles.map(async (artPath) => {
    const {width, height} = await probe(fs.createReadStream(artPath));
    return {
      src: path.relative(artPath, staticPath),
      width,
      height
    }
  }))
}

export const getStaticProps = async (context: GetStaticPropsContext) => {
  const allBlocks = await getAllBlockJson();
  const allArt = await getAllArt();

  return {
    props: {allBlocks, allArt}
  }
}

export const BlocklyDataContext = createContext<InferGetStaticPropsType<typeof getStaticProps> | null>(null)

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
    <BlocklyDataContext.Provider value={props}>
      <div className={styles.cardEditorContainer}>
        <LoadableComponent/>
      </div>
    </BlocklyDataContext.Provider>
  </Layout>
}

export default CardEditor