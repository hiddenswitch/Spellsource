import React, { useState } from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import { graphql, Link, navigate, useStaticQuery } from 'gatsby'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly, { BlockSvg, FieldLabelSerializable, Workspace, WorkspaceSvg } from 'blockly'
import { filter, has, isArray, map } from 'lodash'
import recursiveOmitBy from 'recursive-omit-by'
import AceEditor from 'react-ace'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import { Form } from 'react-bootstrap'
import { useIndex } from '../hooks/use-index'
import BlocklyWorkspace from 'react-blockly/dist-modules/BlocklyWorkspace'

class FieldLabelSerializableHidden extends FieldLabelSerializable {
  constructor (opt_value, opt_validator, opt_config) {
    super(opt_value, opt_validator, opt_config)
  }

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
          baseAttack
          baseHp
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

  const [heroClassColors, setHeroClassColors] = useState({
    ANY: '#A6A6A6'
  })
  const [code, setCode] = useState(``)
  const [inited, setInited] = useState(false)

  const [query, setQuery] = useState(``)
  const [checked, setChecked] = useState(false)
  const [results, setResults] = useState([])

  const index = useIndex()

  if (!inited) {
    Blockly.Blocks = {} //we don't use any of the default Blockly blocks
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
      let card = edge.node
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
              'type': type,
              'message0': card.name,
              'output': 'HeroClass',
              'colour': color
            })
            this.data = card.heroClass
          }
        }
      }
    })

    //second pass through to actually get the cards
    data.allCard.edges.forEach(edge => {
      let card = edge.node
      let type = 'ExternalCard_' + card.id
      if (has(Blockly.Blocks, type)) {
        return
      }
      if (card.baseManaCost != null
        && heroClassColors.hasOwnProperty(card.heroClass)) { //this check if it's *really* collectible
        let color = heroClassColors[card.heroClass]
        Blockly.Blocks[type] = {
          init: function () {
            this.jsonInit({
              'type': type,
              'args0': [],
              'message0': cardMessage(card),
              'output': 'Card',
              'colour': color
            })
            this.data = card.id
          }
        }
      }
    })
  }

  function getToolboxCategories (onlyCategory = null) {
    let i = -1
    return data.toolbox.BlockCategoryList.map(({
      BlockTypePrefix, CategoryName, ColorHex
    }) => {
      i++
      if (!!onlyCategory && CategoryName !== onlyCategory) {
        return toolboxCategories[i] //my attempt to reduce the runtime a bit
      }
      let blocks = []
      if (!!BlockTypePrefix) {
        for (let blocksKey in Blockly.Blocks) {
          if (!blocksKey.endsWith('SHADOW') && blocksKey.startsWith(BlockTypePrefix)) {
            blocks.push({ type: blocksKey })
          } else if (CategoryName === 'Cards' && blocksKey.startsWith('RealCard')) {
            blocks.push({ type: blocksKey })
          }
        }
      } else if (CategoryName === 'Search Results') {
        results.forEach(value => {
          blocks.push({ type: value.id })
        })
      }
      let button = []
      if (CategoryName === 'Cards') {
        button[0] = {
          text: 'Find External Card Block',
          callbackKey: 'findCard'
        }
        button[1] = {
          text: 'Add External Card to Workspace',
          callbackKey: 'importCard'
        }
      }

      return {
        name: CategoryName,
        blocks: blocks,
        colour: ColorHex,
        button: button
      }
    })
  }

  const [toolboxCategories, setToolboxCategories] = useState(getToolboxCategories())

  function extendedJsonInit (thisBlock, block) {
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
          const arg = block['args' + i.toString()][j]
          if (!!arg) {
            const shadow = arg.shadow
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
                shadowBlock.previousConnection : shadowBlock.outputConnection
              thisBlock.getInput(arg.name).connection.connect(connection)
              shadowBlock.initSvg()
            }
          }
        }
      }
    }
  }

  function onWorkspaceChanged (workspace) {
    if (!inited) {
      initializeWorkspace(workspace)
    }

    const cardScript = WorkspaceUtils.workspaceToCardScript(workspace)
    // Generate the blocks that correspond to the cards in the workspace
    let cardsStillInUse = []
    let update = false
    if (isArray(cardScript)) {
      cardScript.forEach(card => {
        if (createCard(card, workspace, cardsStillInUse)) {
          update = true
        }
      })
    } else if (createCard(cardScript, workspace, cardsStillInUse)) {
      update = true
    }
    for (let blocksKey in Blockly.Blocks) {
      if (blocksKey.startsWith('RealCard') && !cardsStillInUse.includes(blocksKey)) {
        delete Blockly.Blocks[blocksKey]
        update = true
      }
    }
    setCode(JSON.stringify(cardScript, null, 2))
    if (update) {
      setToolboxCategories(getToolboxCategories('Cards'))
    }
  }

  function createCard (card, workspace, cardsStillInUse) {
    if (!!card && !!card.name) {
      let cardId = card.type.toLowerCase()
        + '_'
        + card.name.toLowerCase().replace(' ', '_')
      let type = 'RealCard_' + cardId
      let color = '#888888'
      if (!!card.heroClass) {
        color = heroClassColors[card.heroClass]
      }
      let block = {
        init: function () {
          this.jsonInit({
            'type': type,
            'message0': cardMessage(card),
            'output': 'Card',
            'colour': color
          })
          this.data = cardId
        }
      }
      cardsStillInUse.push(type)
      if (Blockly.Blocks[type] !== block) {
        Blockly.Blocks[type] = block
        return true
      }
      return false
    }

  }

  function cardMessage(card) {
    let ret = '(' + card.baseManaCost + ') ';
    if (card.type === 'MINION') {
      ret += card.baseAttack + '/' + card.baseHp;
    } else {
      ret += card.type.replace('_', ' ')
        .split(' ')
        .map(w => w[0].toUpperCase() + w.substr(1).toLowerCase())
        .join(' ')
    }
    ret += ' ' + card.name;
    return ret
  }

  function initializeWorkspace (workspace) {
    workspace.registerButtonCallback('findCard', () => {
      alert('Coming "Soon"')
    })
    workspace.registerButtonCallback('importCard', () => {
      alert('Coming "Soon"')
    })

    workspace.addChangeListener((event) => {
      console.log(event.type)
    })

    setInited(true)
  }

  function generateStarter(workspace, card) {
    let block = workspace.newBlock('Starter_' + card.type)
    ['baseManaCost', 'name', 'baseAttack', 'baseHp', 'heroClass', 'rarity', 'description'].forEach(arg => {
      if (!!card[arg]) {
        block.getField(arg).setValue(card[arg])
      }
    })

    let lowestBlock = block

    if (card.description.toLowerCase().includes('opener')) {
      let extraBlock = workspace.newBlock('Property_opener1')
      extraBlock.setCommentText('Your description contained "Opener" so we thought you might need this property')
      extraBlock.previousConnection.connect(lowestBlock.nextConnection)
      lowestBlock = extraBlock
    }

    if (card.description.toLowerCase().includes('aftermath')) {
      let extraBlock = workspace.newBlock('Property_aftermath')
      extraBlock.setCommentText('Your description contained "Aftermath" so we thought you might need this property')
      extraBlock.previousConnection.connect(lowestBlock.nextConnection)
      lowestBlock = extraBlock
    }

    if (card.description.toLowerCase().includes('whenever')) {
      let extraBlock = workspace.newBlock('Property_triggers')
      extraBlock.setCommentText('Your description contained "Whenever" so we thought you might need this property')
      extraBlock.previousConnection.connect(lowestBlock.nextConnection)
      lowestBlock = extraBlock
    }

    return block
  }

  // update input value
  const updateQuery = event => {
    setQuery(event.target.value)
  }

  const handleSearchResults = event => {
    if (event.target.value.length === 0) {
      setResults([])
    }
    setToolboxCategories(getToolboxCategories('Search Results'))
    if (event.target.value.length > 0) {
      Blockly.getMainWorkspace().getToolbox().selectFirstCategory()
      Blockly.getMainWorkspace().getToolbox().refreshSelection()
    } else {
      Blockly.getMainWorkspace().getToolbox().clearSelection()
    }
  }

  const search = evt => {
    const query = evt.target.value
    setQuery(query)
    setResults(index
        // Query the index with search string to get an [] of IDs
        .search(query, { expand: true }) // accept partial matches
        .map(({ ref }) => index.documentStore.getDoc(ref))
        .filter(doc => doc.nodeType === 'Block' || (doc.nodeType === 'Card' && checked
          && heroClassColors.hasOwnProperty(doc.heroClass) && !!doc.baseManaCost))
        .map(doc => {
          if (doc.nodeType === 'Card') {
            return {
              id: 'ExternalCard_' + doc.id
            }
          }
          return doc
        })
        .slice(0, 20)
      // map over each ID and return full document
    )
  }

  return (<span>
    <Form inline>
      <Form.Control type="text" placeholder={'Search blocks'}
                    value={query}
                    onChange={e => {
                      updateQuery(e)
                      search(e)
                      handleSearchResults(e)
                    }}
                    style={{width: "50%"}}
      />
      <Form.Check style={{display: "inline"}}>
        <Form.Check.Input onChange={e => {
          setChecked(!checked)
          search({target: {value: query}})
          handleSearchResults({target: {value: query}})
        }}
          value={checked} style={
          {
            height: "15px",
            width: "15px",
            webkitAppearance: "checkbox"
          }
        }
        />
        <Form.Check.Label> Show External Card Blocks</Form.Check.Label>
      </Form.Check>
    </Form>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.codeEditor}
      toolboxCategories={toolboxCategories}
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