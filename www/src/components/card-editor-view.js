import React, {useEffect, useMemo, useRef, useState} from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import {isArray} from 'lodash'
import AceEditor from 'react-ace'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import {Form} from 'react-bootstrap'
import {useIndex} from '../hooks/use-index'
import JsonConversionUtils from '../lib/json-conversion-utils'
import BlocklyMiscUtils from '../lib/blockly-misc-utils'
import useComponentWillMount from '../hooks/use-component-will-mount'
import useBlocklyData from '../hooks/use-blockly-data'

const CardEditorView = () => {
  const defaultCard = true

  const data = useBlocklyData()

  const heroClassColors = useMemo(() => BlocklyMiscUtils.getHeroClassColors(data))
  const [code, setCode] = useState(``)
  const [query, setQuery] = useState(``)
  const [showCatalogueBlocks, setShowCatalogueBlocks] = useState(false)
  const [showBlockComments, setShowBlockComments] = useState(true)
  const [results, setResults] = useState([])
  const index = useIndex()
  const blockCommentsTooltip = 'Toggles the helpful/informational comments that display on certain blocks in the toolbox'
  const catalogueBlocksTooltip = 'Toggles whether the blocks for real cards from the catalogue show up in search'

  // Run once before the workspace has been created
  useComponentWillMount(() => {
    BlocklyMiscUtils.initializeBlocks(data)
  })

  // Run once after the workspace has been created
  useEffect(() => {
    const workspace = Blockly.getMainWorkspace()
    workspace.options.disable = false
    const importCardCallback = () => {
      let p = prompt('Input the name of the card (or the wiki page URL / Card ID for more precision)')
      generateCard(p)
      workspace.getToolbox().clearSelection()
      setToolboxCategories(getToolboxCategories())
    }
    workspace.registerButtonCallback('importCard', importCardCallback)
    const changeListenerCallback = (event) => {
      if (event.type === Blockly.Events.UI && event.element === 'category') {
        if (event.newValue === 'Targets') {
          // TODO: change listener callback work
        }
      }
    }
    workspace.addChangeListener(changeListenerCallback)

    if (defaultCard) {
      setTimeout(() => {
        const array = ["Daring Duelist", "Ninja Aspirants", "Redhide Butcher",
          "Sly Conquistador", "Stormcloud Assailant", "Peacock Mystic"]
        generateCard(array[Math.floor(Math.random() * array.length)])
        Blockly.getMainWorkspace().getTopBlocks(true)[0].setCommentText("This card was imported automatically as an example.")
      }, 100)
    }

    return () => {
      workspace.removeButtonCallback('importCard')
      workspace.removeChangeListener(changeListenerCallback)
    }
  }, [])

  function getToolboxCategories(onlyCategory = null) {
    let index = -1
    return data.toolbox.BlockCategoryList.map((
      {
        BlockTypePrefix, CategoryName, ColorHex, Subcategories
      }) => {
      index++
      if (!!onlyCategory && CategoryName !== onlyCategory) {
        return toolboxCategories[index] //my attempt to reduce the runtime a bit
      }
      let blocks = []
      if (!!BlockTypePrefix) {
        for (let blocksKey in Blockly.Blocks) {
          if ((!blocksKey.endsWith('SHADOW') && blocksKey.startsWith(BlockTypePrefix))
            || (CategoryName === 'Cards' && blocksKey.startsWith('WorkspaceCard'))) {
            blocks.push({
              type: blocksKey,
              values: shadowBlockJsonCreation(blocksKey),
              next: blocksKey.startsWith('Starter') && !!Blockly.Blocks[blocksKey].json.nextStatement ?
                {type: 'Property_SHADOW', shadow: true}
                : undefined
            })
          }
        }

        if (!JsonConversionUtils.blockTypeColors[BlockTypePrefix]) {
          JsonConversionUtils.blockTypeColors[BlockTypePrefix] = ColorHex
        }
      } else if (CategoryName === 'Search Results') {
        results.forEach(value => {
          blocks.push({
            type: value.id,
            values: shadowBlockJsonCreation(value.id),
            next: value.id.startsWith('Starter') && !!Blockly.Blocks[value.id].json.nextStatement ?
              {type: 'Property_SHADOW', shadow: true}
              : undefined
          })
        })
      }
      let button = []
      if (CategoryName === 'Cards') {
        button[0] = {
          text: 'Import Catalogue Card Code',
          callbackKey: 'importCard'
        }
      }

      if (!!Subcategories && isArray(Subcategories)) {
        let categories = []

        for (let category of Subcategories) {
          categories[category] = {
            name: category,
            blocks: [],
            colour: ColorHex
          }
        }

        for (let block of blocks) {
          let subcategory = Blockly.Blocks[block.type].json?.subcategory
          if (!!subcategory) {
            if (subcategory.includes(',')) {
              for (let splitKey of subcategory.split(',')) {
                if (categories[splitKey] != null) {
                  categories[splitKey].blocks.push(block)
                }
              }
            } else if (categories[subcategory] != null) {
              categories[subcategory].blocks.push(block)
            } else {
              categories['Misc'].blocks.push(block)
            }
          } else {
            categories['Misc'].blocks.push(block)
          }
        }


        return {
          name: CategoryName,
          colour: ColorHex,
          categories: Object.values(categories)
        }

      } else return {
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
                  fields[field.name] = field.valueI || field.valueS || field.valueB
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

  function onWorkspaceChanged(workspace) {
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
      if (blocksKey.startsWith('WorkspaceCard') && !cardsStillInUse.includes(blocksKey)) {
        delete Blockly.Blocks[blocksKey]
        update = true
      }
    }
    setCode(JSON.stringify(cardScript, null, 2))
    if (update) {
      setToolboxCategories(getToolboxCategories('Cards'))
    }
  }

  function createCard(card, workspace, cardsStillInUse) {
    if (!!card && !!card.name && !!card.type) {
      let cardType = !!card.secret ? 'SECRET' : !!card.quest ? 'QUEST' : card.type
      let cardId = cardType.toLowerCase()
        + '_'
        + card.name
          .toLowerCase()
          .replace(' ', '_')
          .replace(',', '')
          .replace("'", '')
      if (card.type === 'MINION' && card.collectible === false || card.collectible === 'FALSE') {
        cardId.replace('minion_', 'token_')
      }
      if (card.type === 'CLASS') {
        cardId = 'class_' + card.heroClass.toLowerCase()
      }
      let type = 'WorkspaceCard_' + cardId
      let color = '#888888'
      if (!!card.heroClass) {
        color = heroClassColors[card.heroClass]
      }
      let block = {
        init: function () {
          this.jsonInit({
            'type': type,
            'message0': BlocklyMiscUtils.cardMessage(card),
            'output': 'Card',
            'colour': color
          })
          this.data = cardId
        }
      }
      if (!Blockly.Blocks[type.replace('WorkspaceCard_', 'CatalogueCard_')]) {
        cardsStillInUse.push(type)
      }
      if (Blockly.Blocks[type] !== block) {
        Blockly.Blocks[type] = block
        return true
      }
      return false
    }

  }

  function generateCard(p) {
    let cardId = null
    let card = null

    if (!p) {
      return
    }

    if (p.includes('{')) {
      card = JSON.parse(p)
    } else if (p.includes('www')) {
      cardId = p.split('cards/')[1]
    } else if (p.includes('_')) {
      cardId = p
    } else {
      for (let edge of data.allCard.edges) {
        if (edge.node.name.toLowerCase() === p.toLowerCase()) {
          cardId = edge.node.id
          break
        }
      }
    }
    if (!!cardId) {
      for (let edge of data.allFile.edges) {
        let node = edge.node
        if (node.name === cardId) {
          card = JSON.parse(node.internal.content)
        }
      }
    }

    if (!card) {
      return
    }

    JsonConversionUtils.generateCard(Blockly.getMainWorkspace(), card)
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
    const workspace = Blockly.getMainWorkspace()
    if (event.target.value.length > 0) {
      workspace.getToolbox().selectFirstCategory()
      workspace.getToolbox().refreshSelection()
    } else {
      workspace.getToolbox().clearSelection()
    }
  }

  const search = evt => {
    const query = evt.target.value
    setQuery(query)
    setResults(index
        // Query the index with search string to get an [] of IDs
        .search(query, {expand: true}) // accept partial matches
        .map(({ref}) => index.documentStore.getDoc(ref))
        .filter(doc => doc.nodeType === 'Block' || (doc.nodeType === 'Card' && showCatalogueBlocks
          && heroClassColors.hasOwnProperty(doc.heroClass) && doc.hasOwnProperty('baseManaCost')))
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
      <Form.Control type="text" placeholder={'Search blocks'}
                    value={query}
                    onChange={e => {
                      updateQuery(e)
                      search(e)
                      handleSearchResults(e)
                    }}
                    style={{width: '40%'}}
      />
      <Form.Check style={{display: 'inline'}}>
        <Form.Check.Input defaultChcked={showCatalogueBlocks} onChange={e => {
          setShowCatalogueBlocks(!showCatalogueBlocks)
          Blockly.getMainWorkspace().getToolbox().clearSelection()
          if (query.length > 0) {
            search({target: {value: query}})
            handleSearchResults({target: {value: query}})
          }
        }} value={showCatalogueBlocks} title={catalogueBlocksTooltip} style={
          {
            height: '15px',
            width: '15px',
            webkitAppearance: 'checkbox'
          }
        }
        />
        <Form.Check.Label title={catalogueBlocksTooltip}> Search Card Catalogue   </Form.Check.Label>
      </Form.Check>
      <Form.Check style={{display: 'inline'}}>
        <Form.Check.Input defaultChecked={showBlockComments} onChange={e => {
          setShowBlockComments(!showBlockComments)
          Blockly.getMainWorkspace().getToolbox().clearSelection()
          Blockly.getMainWorkspace().hideSpellsourceComments = showBlockComments
        }} value={showBlockComments} title={blockCommentsTooltip} style={
          {
            height: '15px',
            width: '15px',
            webkitAppearance: 'checkbox'
          }
        }
        />
        <Form.Check.Label title={blockCommentsTooltip}> Show Block Comments</Form.Check.Label>
      </Form.Check>
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
      editorProps={{$blockScrolling: true}}
    />
  </span>)
}

export default CardEditorView