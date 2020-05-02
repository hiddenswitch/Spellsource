import React from 'react'
import Layout from '../components/creative-layout'
import { useStaticQuery, graphql } from 'gatsby'
import styles from './card-editor.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import { has, isObject } from 'lodash'
import recursiveOmitBy from 'recursive-omit-by'

const CardEditor = () => {
  const data = useStaticQuery(graphql`
  query {
    allBlock {
      edges {
        node {
          id
          args0 {
            check
            int
            name
            type
          }
          args1 {
            check
            int
            max
            min
            name
            type
            value
          }
          args2 {
            check
            name
            type
            value
          }
          args3 {
            check
            name
            type
          }
          args4 {
            check
            name
            type
          }
          colour
          message0
          message1
          message2
          message4
          message3
          nextStatement
          output
          previousStatement
          type
        }
      }
    }
  }`)

  data.allBlock.edges.forEach(edge => {
    if (has(Blockly.Blocks, edge.node.type)) {
      return
    }

    const block = recursiveOmitBy(edge.node, ({ node }) => node === null)

    Blockly.Blocks[block.type] = {
      init: function () {
        this.jsonInit(block)
      }
    }
  })

  return <Layout>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceConfiguration={{
        grid: {
          spacing: 20,
          length: 3,
          colour: '#ccc',
          snap: true
        }
      }}
      wrapperDivClassName={styles.fillHeight}
      toolboxBlocks={data.allBlock.edges.map(edge => {return { type: edge.node.type }})}/>
  </Layout>
}

export default CardEditor