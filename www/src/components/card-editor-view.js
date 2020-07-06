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
import JsonConversionUtils from '../lib/json-conversion-utils'

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
          description
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

      addBlock(block)
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
        let block = {
          'type': type,
          'message0': card.name,
          'output': 'HeroClass',
          'colour': color,
          'data': card.heroClass
        }
        addBlock(block)
      }
    })

    //second pass through to actually get the cards
    data.allCard.edges.forEach(edge => {
      let card = edge.node
      let type = 'CatalogueCard_' + card.id
      if (has(Blockly.Blocks, type)) {
        return
      }
      if (card.baseManaCost != null
        && heroClassColors.hasOwnProperty(card.heroClass)) { //this check if it's *really* collectible
        let color = heroClassColors[card.heroClass]
        let block = {
          'type': type,
          'args0': [],
          'message0': cardMessage(card),
          'output': 'Card',
          'colour': color,
          'data': card.id
        }
        addBlock(block)
      }
    })
  }

  function addBlock(block) {
    Blockly.Blocks[block.type] = {
      init: function () {
        this.jsonInit(block)
        if (!!block.data) {
          this.data = block.data
        }
        if (!!block.hat) {
          this.hat = block.hat
        }
        if (block.type.endsWith('SHADOW')) {
          this.setMovable(false)
        }
      }
    }
    Blockly.Blocks[block.type].json = block
    JsonConversionUtils.addBlockToMap(block)
  }

  function getToolboxCategories (onlyCategory = null) {
    let index = -1
    return data.toolbox.BlockCategoryList.map(({
      BlockTypePrefix, CategoryName, ColorHex
    }) => {
      index++
      if (!!onlyCategory && CategoryName !== onlyCategory) {
        return toolboxCategories[index] //my attempt to reduce the runtime a bit
      }
      let blocks = []
      if (!!BlockTypePrefix) {
        for (let blocksKey in Blockly.Blocks) {
          if ((!blocksKey.endsWith('SHADOW') && blocksKey.startsWith(BlockTypePrefix))
          || (CategoryName === 'Cards' && blocksKey.startsWith('RealCard'))) {
            blocks.push({
              type: blocksKey,
              values: shadowBlockJsonCreation(blocksKey),
              next: blocksKey.startsWith('Starter') ?
                {type:'Property_SHADOW', shadow:true}
                : undefined
            })
          }
        }
      } else if (CategoryName === 'Search Results') {
        results.forEach(value => {
          blocks.push({
            type: value.id,
            values: shadowBlockJsonCreation(value.id),
            next: value.id.startsWith('Starter') ?
              {type:'Property_SHADOW', shadow:true}
              : undefined
          })
        })
      }
      let button = []
      if (CategoryName === 'Cards') {
        button[0] = {
          text: 'Add External Card Code to Workspace',
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

  //Turns our own json formatting for shadow blocks into the formatting
  //that's used for specifying toolbox categories (recursively)
  function shadowBlockJsonCreation(type) {
    let block = Blockly.Blocks[type]
    let values = {}
    if (!!block && !!block.json) {
      let json = block.json
      for (let i = 0; i < 10; i++) {
        if (!!json['args' + i.toString()]) {
          for (let j = 0; j < 10; j++) {
            const arg = json['args' + i.toString()][j]
            if (!!arg && !!arg.shadow) {
              let fields = {}
              if (!!arg.shadow.fields) {
                for (let field of arg.shadow.fields) {
                  fields[field.name] = field.valueI
                }
              }
              values[arg.name] = {
                type: arg.shadow.type,
                shadow: !arg.shadow.notActuallyShadow,
                fields: fields,
                values: shadowBlockJsonCreation(arg.shadow.type)
              }
            }
          }
        }
      }
    }
    return values
  }


  const [toolboxCategories, setToolboxCategories] = useState(getToolboxCategories())

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
      let p = prompt("Input the name of the card (or the wiki page URL for more precision)")
      let cardId = null
      let card = null
      if (p.includes('{')) {
        card = JSON.parse(p)
      } else if (p.includes('www')) {
        cardId = p.split('cards/')[1]
      } else {
        for (let edge of data.allCard.edges) {
          let card = edge.node
          if (card.name.toLowerCase() === p.toLowerCase()) {
            cardId = card.id
            break
          }
        }
      }
      if (!!cardId) {
        for (let edge of data.allCard.edges) {
          if (edge.node.id === cardId) {
            card = edge.node
            break
          }
        }
      }


      if (!!card) {
        console.log(card)
      }

      JsonConversionUtils.generateCard(Blockly.getMainWorkspace(), card)
      Blockly.getMainWorkspace().getToolbox().clearSelection()
    })

    workspace.addChangeListener((event) => {
      console.log(event.type)
    })

    setInited(true)
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
              id: 'CatalogueCard_' + doc.id
            }
          }
          return doc
        })
        .slice(0, 20)
      // map over each ID and return full document
    )
  }

  return (<span>
    <Form inline onSubmit={(event) => event.preventDefault()}>
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
          if (query.length > 0) {
            search({target: {value: query}})
            handleSearchResults({target: {value: query}})
          }
        }}
          value={checked}
          style={
          {
            height: "15px",
            width: "15px",
            webkitAppearance: "checkbox"
          }
        }
        />
        <Form.Check.Label> Show Card Catalogue Blocks</Form.Check.Label>
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