import React from 'react'
import Loadable from 'react-loadable'
import Layout from '../components/card-editor-layout'
import * as BlocklyMiscUtils from "../lib/blockly-misc-utils";

const LoadableComponent = Loadable.Map({
  loader: {
    Blockly: () => import('blockly'),
    CardEditorWorkspace: () => import('../components/card-editor-workspace')
  },
  delay: 300,
  loading () {return (<span>Loading</span>)},
  render (loaded) {
    const Blockly = loaded.Blockly
    const CardEditorWorkspace = loaded.CardEditorWorkspace.default

    BlocklyMiscUtils.loadableInit(Blockly)
    
    return <CardEditorWorkspace defaultCard={true}/>
  }
})

const CardEditorExample = () => {
  return <Layout>
    <LoadableComponent/>
  </Layout>
}

export default CardEditorExample
