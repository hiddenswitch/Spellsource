import React, { useState } from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import { graphql, useStaticQuery } from 'gatsby'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly, { FieldLabelSerializable } from 'blockly'
import { filter, has, isArray, map } from 'lodash'
import recursiveOmitBy from 'recursive-omit-by'
import AceEditor from 'react-ace'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'

class FieldLabelSerializableHidden extends FieldLabelSerializable {
  constructor (opt_value, opt_validator, opt_config) {super(opt_value, opt_validator, opt_config)}

  static fromJson (options) {
    const field = new FieldLabelSerializableHidden()
    field.setValue(options.value)
    return field
  }

  getSize () {
    let size = super.getSize()
    size.width = -10
    return size
  }

  getDisplayText_ () {
    return ''
  }
}

Blockly.fieldRegistry.register('field_label_serializable_hidden', FieldLabelSerializableHidden)
Blockly.HSV_SATURATION = .65

const CardEditorView = () => {
  const data = useStaticQuery(graphql`
  query {
    toolbox {
      BlockCategoryList {
        BlockTypePrefix
        CategoryName
        ColorHex
      }
    }
    allBlock {
      edges {
        node {
          id
          args {
            i
            args {
              type
              check
              name
              valueS
              valueI
              min
              max
              int
              text
              options
              shadow {
                type
                fields {
                  name
                  valueS
                  valueI
                }
              }
            }
          }
          inputsInline
          colour
          messages
          nextStatement
          output
          previousStatement
          type
          data
        }
      }
    }
  }`)

  function getInitialToolboxCategories () {
    return data.toolbox.BlockCategoryList.map(({
      BlockTypePrefix, CategoryName, ColorHex
    }) => {
      return {
        name: CategoryName,
        blocks: filter(data.allBlock.edges, edge => edge.node.type.startsWith(BlockTypePrefix)
        && !edge.node.type.endsWith('SHADOW'))
          .map(edge => {return { type: edge.node.type }})
      }
    })
  }

  const [code, setCode] = useState(``)
  const [toolboxCategories, setToolboxCategories] = useState(getInitialToolboxCategories())

  // All of our spells, triggers, entity reference enum values, etc.
  data.allBlock.edges.forEach(edge => {
    if (has(Blockly.Blocks, edge.node.type)) {
      return
    }

    const block = recursiveOmitBy(edge.node, ({ node }) => node === null)

    // Patch back in values from union type
    if (!!block.args) {
      block.args.forEach(args => {
        args.args.forEach(arg => {
          if (!!arg.valueI) {
            arg.value = arg.valueI
            delete arg.valueI
          }
          if (!!arg.valueS) {
            arg.value = arg.valueS
            delete arg.valueS
          }
        })

        block['args' + args.i.toString()] = args.args
      })
      delete block.args
    }

    if (!!block.messages) {
      block.messages.forEach((message, i) => {
        block['message' + i.toString()] = message
      })
      delete block.messages
    }

    Blockly.Blocks[block.type] = {
      init: function () {
        this.jsonInit(block)
        if (!!block.data) {
          this.data = block.data
        }
        //init shadow blocks
        for (let i = 0; i < 10; i++) {
          if (!!block['args' + i.toString()]) {
            for (let j = 0; j < 10; j++) {
              const arg = block['args' + i.toString()][j];
              if (!!arg) {
                const shadow = arg.shadow;
                if (!!shadow) {
                  let shadowBlock = this.workspace.newBlock(shadow.type)
                  shadowBlock.setShadow(true)
                  if (!!shadow.fields) {
                    for (let field of shadow.fields) {
                      if (!!field.valueI) {
                        shadowBlock.setFieldValue(field.valueI, field.name)
                      }
                      if (!!field.valueS) {
                        shadowBlock.setFieldValue(field.valueS, field.name)
                      }
                    }
                  }
                  const connection = arg.type.endsWith('statement') ?
                    shadowBlock.previousConnection: shadowBlock.outputConnection
                  this.getInput(arg.name).connection.connect(connection)
                  shadowBlock.initSvg()
                }
              }
            }
          }
        }
      }
    }
  })

  function onWorkspaceChanged (workspace) {
    const cardScript = WorkspaceUtils.workspaceToCardScript(workspace)
    // Generate the blocks that correspond to the cards in the workspace
    if (isArray(cardScript)) {
      map(filter(cardScript, topLevelElement => !!topLevelElement && !!topLevelElement.name), card => {
        const cardId = card.name.toLowerCase().replace(' ', '')
        return {
          cardId: cardId,
          init: function () {
            this.jsonInit({
              'type': 'Card_' + cardId,
              'args0': [],
              'message0': 'card named ' + card.name,
              'output': 'Card',
              'data': cardId
            })
          }
        }
      }).forEach(card => Blockly.Blocks['Card_' + card.cardId] = card)
    }
    workspace.refreshToolboxSelection()
    setCode(JSON.stringify(cardScript, null, 2))
  }

  return (<span>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.codeEditor}
      toolboxCategories={toolboxCategories}/>
    <AceEditor
      width={'100%'}
      mode="json"
      theme="github"
      setOptions={{
        'wrap': true
      }}
      readOnly={true}
      value={code}
      editorProps={{ $blockScrolling: true }}
    /></span>)
}

export default CardEditorView