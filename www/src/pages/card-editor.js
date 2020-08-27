import React from 'react'
import Loadable from 'react-loadable'
import Layout from '../components/card-editor-layout'

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

    Blockly.Css.register([
      /* eslint-disable indent */
      '.blocklyCommentTextarea {',
      'color: black;',
      'caret-color: black;',
      'font-size: 12pt;',
      'background-color: lightgray;',
      '}'
      /* eslint-enable indent */
    ]);

    setTimeout(() => {
      const all = Blockly.Workspace.getAll()
      for (let i = 0; i < all.length; i++) {
        const workspace = all[i]
        if (!workspace.parentWorkspace && workspace.rendered) {
          Blockly.svgResize(workspace)
        }
      }
    }, 1)
    return <CardEditorView/>
  }
})

const CardEditor = () => {
  return <Layout>
    <LoadableComponent/>
  </Layout>
}

export default CardEditor