import React, {useState} from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import {graphql, useStaticQuery} from 'gatsby'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly, {FieldLabelSerializable} from 'blockly'
import {filter, has, isArray, map} from 'lodash'
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
        Custom
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
              valueB
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
                notActuallyShadow
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
          hat
        }
      }
    }
    allCard {
      edges {
        node {
          id
          name
          baseManaCost
          heroClass
          type
          collectible
          art {
            primary {
              r
              g
              b
            }
          }
        }
      }
    }
  }`)

  const [dummyCardsWorkspace, setdummyCardsWorkspace] = useState(new Blockly.Workspace())
  const [heroClassColors, setHeroClassColors] = useState({
    ANY: "#A6A6A6"
  })
  const [code, setCode] = useState(``)
  const [toolboxCategories, setToolboxCategories] = useState({})
  const [inited, setInited] = useState(false)

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
          if (arg.hasOwnProperty('valueB')) {
            //arg.value = arg.valueB
            //gotta do this because it seems like the block -> xml conversion hates booleans
            if (arg.valueB === true) {
              arg.value = 'TRUE'
            } else if (arg.valueB === false) {
              arg.value = 'FALSE'
            }
            delete arg.valueB
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
        extendedJsonInit(this, block)
      }
    }
  })

  /**
   * first pass through the card catalogue to figure out all the collectible
   * hero classes and their colors
   */
  data.allCard.edges.forEach(edge => {
    let card = edge.node;
    let type = 'HeroClass_' + card.heroClass
    if (has(Blockly.Blocks, type)) {
      return
    }
    if (card.type === 'CLASS' && card.collectible) {
      let color = Blockly.utils.colour.rgbToHex(
        card.art.primary.r * 255,
        card.art.primary.g * 255,
        card.art.primary.b * 255
      )

      heroClassColors[card.heroClass] = color
      Blockly.Blocks[type] = {
        init: function () {
          this.jsonInit({
            "type": type,
            "message0": card.name,
            "output": "HeroClass",
            "colour": color
          })
          this.data = card.heroClass
        }
      }
    }
  })

  //second pass through to actually get the cards
  data.allCard.edges.forEach(edge => {
    let card = edge.node;
    let type = 'ExternalCard_' + card.id
    if (has(Blockly.Blocks, type)) {
      return
    }
    if (card.baseManaCost != null
      && heroClassColors.hasOwnProperty(card.heroClass)) { //this check if it's *really* collectible
      let color = heroClassColors[card.heroClass]
      let message = card.type
        .replace('_', ' ')
        .split(' ')
        .map(w => w[0].toUpperCase() + w.substr(1).toLowerCase())
        .join(' ')
      Blockly.Blocks[type] = {
        init: function () {
          this.jsonInit({
            "type": type,
            "args0": [],
            "message0": message + " (" + card.baseManaCost + ") " + card.name,
            "output": "Card",
            "colour": color
          })
          this.data = card.id
        }
      }
    }
  })

  data.toolbox.BlockCategoryList.map(({
      BlockTypePrefix, CategoryName, ColorHex, Custom
    }) => {
    let blocks = []
    if (!!BlockTypePrefix) {
      for (let blocksKey in Blockly.Blocks) {
        if (blocksKey.startsWith(BlockTypePrefix) && !blocksKey.endsWith('SHADOW')) {
          blocks.push({type: blocksKey})
        }
      }
    }
    let type
    if (CategoryName === 'Search') {
      type = 'search'
    }

    toolboxCategories[CategoryName] = {
      name: CategoryName,
      blocks: blocks,
      colour: ColorHex,
      custom: Custom,
      type: type
    }

  })

  function extendedJsonInit(thisBlock, block) {
    thisBlock.jsonInit(block)
    if (!!block.data) {
      thisBlock.data = block.data
    }
    if (!!block.hat) {
      thisBlock.hat = block.hat
    }
    if (!thisBlock.workspace.isFlyout) {
      return
    }
    //init shadow blocks
    for (let i = 0; i < 10; i++) {
      if (!!block['args' + i.toString()]) {
        for (let j = 0; j < 10; j++) {
          const arg = block['args' + i.toString()][j];
          if (!!arg) {
            const shadow = arg.shadow;
            if (!!shadow) {
              let shadowBlock = thisBlock.workspace.newBlock(shadow.type)
              if (shadow.notActuallyShadow) {
                shadowBlock.setMovable(false)
              } else {
                shadowBlock.setShadow(true)
              }
              if (!!shadow.fields) {
                for (let field of shadow.fields) {
                  if (!!field.valueI) {
                    shadowBlock.setFieldValue(field.valueI, field.name)
                  }
                  if (!!field.valueS) {
                    shadowBlock.setFieldValue(field.valueS, field.name)
                  }
                  if (!!field.valueB) {
                    shadowBlock.setFieldValue(field.valueB, field.name)
                  }
                }
              }
              const connection = arg.type.endsWith('statement') ?
                shadowBlock.previousConnection: shadowBlock.outputConnection
              thisBlock.getInput(arg.name).connection.connect(connection)
              shadowBlock.initSvg()
            }
          }
        }
      }
    }
  }

  function onWorkspaceChanged(workspace) {
    if (!inited) {
      initializeWorkspace(workspace)
    }

    const cardScript = WorkspaceUtils.workspaceToCardScript(workspace)
    // Generate the blocks that correspond to the cards in the workspace
    let cardsStillInUse = []
    if (isArray(cardScript)) {
      cardScript.forEach(card => {
        createCard(card, workspace, cardsStillInUse)
      })
    } else {
      createCard(cardScript, workspace, cardsStillInUse)
    }
    for (let blocksKey in Blockly.Blocks) {
      if (blocksKey.startsWith('RealCard') && !cardsStillInUse.includes(blocksKey)) {
        dummyCardsWorkspace.getBlocksByType(blocksKey)[0].dispose(false)
        delete Blockly.Blocks[blocksKey]
      }
    }
    setCode(JSON.stringify(cardScript, null, 2))
  }

  function createCard(card, workspace, cardsStillInUse) {
    if (!!card && !!card.name) {
      let cardId = card.type.toLowerCase()
        + '_'
        + card.name.toLowerCase().replace(' ', '_')
      let type = 'RealCard_' + cardId;
      let color = "#888888"
      if (!!card.heroClass){
        color = heroClassColors[card.heroClass]
      }
      let message = card.type
        .replace('_', ' ')
        .split(' ')
        .map(w => w[0].toUpperCase() + w.substr(1).toLowerCase())
        .join(' ')
      Blockly.Blocks[type] = {
        init: function () {
          this.jsonInit({
            "type": type,
            "message0": message + " (" + card.baseManaCost + ") " + card.name,
            "output": "Card",
            "colour": color
          })
          this.data = cardId
        }
      }
      if (dummyCardsWorkspace.getBlocksByType(type).length === 0) {
        dummyCardsWorkspace.newBlock(type)
      } else if (dummyCardsWorkspace.getBlocksByType(type)[0].getColour() !== color) {
        dummyCardsWorkspace.getBlocksByType(type)[0].dispose(false)
        dummyCardsWorkspace.newBlock(type)
      }
      cardsStillInUse.push(type)
    }

  }

  function initializeWorkspace(workspace) {
    workspace.registerToolboxCategoryCallback("CARDS", cardsCategoryCallback)
    workspace.registerButtonCallback("findCard", () => {
      alert('Coming "Soon"')
    })
    workspace.registerButtonCallback("importCard", () => {
      alert('Coming "Soon"')
    })

    toolboxCategories['Cards'].blocks.forEach(block => {
      dummyCardsWorkspace.newBlock(block.type)
    })

    setInited(true)
  }

  function cardsCategoryCallback(workspace) {
    let xmlList = [];
    xmlList.push(Blockly.Xml.textToDom('<button text="Find External Card Block" callbackKey="findCard"></button>'))
    xmlList.push(Blockly.Xml.textToDom('<button text="Add External Card to Workspace" callbackKey="importCard"></button>'))
    dummyCardsWorkspace.getAllBlocks().forEach(block => {
      xmlList.push(Blockly.Xml.blockToDom(block))
    })
    return xmlList
  }

  return (<span>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.codeEditor}
      toolboxCategories={Object.values(toolboxCategories)}
    />
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
    />
  </span>)
}


export default CardEditorView