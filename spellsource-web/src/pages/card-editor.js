import React from 'react'
import Loadable from 'react-loadable'
import Layout from '../components/card-editor-layout'
import BlocklyMiscUtils from "../lib/blockly-misc-utils";
import styles from '../templates/template-styles.module.scss';

const LoadableComponent = Loadable.Map({
  loader: {
    Blockly: () => import('blockly'),
    CardEditorView: () => import('../components/card-editor-view')
  },
  delay: 300,
  loading () {return (<span>Loading</span>)},
  render (loaded) {
    const Blockly = loaded.Blockly
    const CardEditorView = loaded.CardEditorView.default

    BlocklyMiscUtils.loadableInit(Blockly)

    return <CardEditorView defaultCard={false}/>
  }
})

const CardEditor = () => {
  return <Layout>
    <div className={styles.cardEditorContainer}>
      <LoadableComponent/>
    </div>
  </Layout>
}

export default CardEditor