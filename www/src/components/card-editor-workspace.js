import React, {useEffect, useRef, useState, forwardRef} from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import {isArray} from 'lodash'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import {useIndex} from '../hooks/use-index'
import JsonConversionUtils from '../lib/json-conversion-utils'
import BlocklyMiscUtils from '../lib/blockly-misc-utils'
import useComponentWillMount from '../hooks/use-component-will-mount'
import useBlocklyData from '../hooks/use-blockly-data'
import SpellsourceRenderer from '../lib/spellsource-renderer'
import SpellsourceGenerator from "../lib/spellsource-generator";

const CardEditorWorkspace = forwardRef((props, blocklyEditor) => {
  const data = useBlocklyData()
  const [results, setResults] = useState([])
  const index = useIndex()

  const mainWorkspace = () => {
    return blocklyEditor.current.workspace.state.workspace
  }

  // Run once before the workspace has been created
  useComponentWillMount(() => {
    BlocklyMiscUtils.initBlocks(data)
    BlocklyMiscUtils.initHeroClassColors(data)
    BlocklyMiscUtils.initCardBlocks(data)
    Blockly.blockRendering.register('spellsource', SpellsourceRenderer);
    SpellsourceGenerator.generateJavaScript()
  })

  // Run once after the workspace has been created
  useEffect(() => {
    if (props.defaultCard) {
      setTimeout(() => {
        const array = ["Daring Duelist", "Ninja Aspirants", "Redhide Butcher",
          "Sly Conquistador", "Stormcloud Assailant", "Peacock Mystic"]
        generateCard(array[Math.floor(Math.random() * array.length)])
        mainWorkspace().getTopBlocks(true)[0].setCommentText("This card was imported automatically as an example.")
      }, 100)
    }
    mainWorkspace().getTheme().setStartHats(true)
    mainWorkspace().registerButtonCallback('cardsInfo', button => {
      alert("For each card Starter in the workspace, a card block will appear in here that references it. That's how you can create interactions with your custom cards like summoning them or receiving them.")
    })
  }, [])

  useEffect(() => {
    search(props.query)
    handleSearchResults(props.query)
  }, [props.query])

  useEffect(() => {
    BlocklyMiscUtils.switchRenderer(props.renderer, mainWorkspace())
  }, [props.renderer])

  const getToolboxCategories = (onlyCategory = null) => {
    return BlocklyMiscUtils.getToolboxCategories(data.editorToolbox.BlockCategoryList,
      toolboxCategories, onlyCategory, results)
  }

  const [toolboxCategories, setToolboxCategories] = useState(getToolboxCategories())

  const onWorkspaceChanged = (workspace) => {
    const cardScript = WorkspaceUtils.workspaceToCardScript(workspace)
    props.setCode(JSON.stringify(cardScript, null, 2))
    // Generate the blocks that correspond to the cards in the workspace
    if (!workspace.isDragging()) {
      let update = handleWorkspaceCards(workspace, cardScript)
      if (update) {
        setToolboxCategories(getToolboxCategories())
      }
      BlocklyMiscUtils.pluralStuff(workspace)
    }
  }

  const createCard = (card, blockType) => {
    if (!!card && !!card.name && !!card.type) {
      let cardType = !!card.secret ? 'SECRET' : !!card.quest ? 'QUEST' : card.type
      let cardId = cardType.toLowerCase()
        + '_'
        + card.name
          .toLowerCase()
          .replaceAll(' ', '_')
          .replaceAll(',', '')
          .replaceAll("'", '')
      if (card.type === 'MINION' && card.collectible === false || card.collectible === 'FALSE') {
        cardId.replace('minion_', 'token_')
      }
      if (card.type === 'CLASS') {
        cardId = 'class_' + card.heroClass.toLowerCase()
      }
      let color = '#888888'
      if (!!card.heroClass) {
        color = Blockly.heroClassColors[card.heroClass]
      }
      let message = BlocklyMiscUtils.cardMessage(card)
      let json = {
        'type': blockType,
        'message0': '%1',
        'output': 'Card',
        'colour': color,
        'args0': [
          {
            'type': 'field_label',
            'name': 'message',
            'text': message
          }
        ]
      }
      return {
        init: function () {
          this.jsonInit(json)
        },
        data: cardId,
        json: json,
        message: message
      }
    }
  }

  const createClass = (card, blockType) => {
    if (!!card && !!card.heroClass && card.type === 'CLASS') {
      let color = Blockly.utils.colour.rgbToHex(
        card.art.primary.r * 255,
        card.art.primary.g * 255,
        card.art.primary.b * 255
      )
      let message = card.name
      let json = {
        'type': blockType,
        'message0': '%1',
        'output': 'HeroClass',
        'colour': color,
        'args0': [
          {
            'type': 'field_label',
            'name': 'message',
            'text': message
          }
        ]
      }
      if (!!card.art.body?.vertex) {
        Blockly.textColor[color] = Blockly.utils.colour.rgbToHex(
          card.art.body.vertex.r * 255,
          card.art.body.vertex.g * 255,
          card.art.body.vertex.b * 255
        )
      }
      Blockly.heroClassColors[card.heroClass] = color
      return {
        init: function () {
          this.jsonInit(json)
        },
        data: card.heroClass,
        json: json,
        message: message
      }
    }
  }


  const handleWorkspaceCards = (workspace, cardScript) => {
    let anythingChanged = false
    if (!isArray(cardScript)) {
      cardScript = [cardScript]
    }

    let currentCards = []
    for (let blocksKey in Blockly.Blocks) {
      if (blocksKey.startsWith('WorkspaceCard') || blocksKey.startsWith('WorkspaceHeroClass')) {
        currentCards.push(blocksKey)
      }
    }
    let i = 0 //this works because the cardScript also uses the ordered getTopBlocks
    workspace.getTopBlocks(true).forEach(block => {
      if (block.type.startsWith('Starter_')) {
        let card = cardScript[i]

        //if it's a class card, make the class first to init the color
        if (block.type === 'Starter_CLASS') {
          let type = 'WorkspaceHeroClass_' + block.id
          currentCards = currentCards.filter(value => value !== type)
          if (!!Blockly.Blocks[type]) {
            anythingChanged = true
          }
          Blockly.Blocks[type] = createClass(card, type)
        }

        let blockType = 'WorkspaceCard_' + block.id
        currentCards = currentCards.filter(value => value !== blockType)
        if (!!Blockly.Blocks[blockType]) {
          anythingChanged = true
        }
        Blockly.Blocks[blockType] = createCard(card, blockType)
      }
      i++
    })

    currentCards.forEach(card => {
      anythingChanged = true
      delete Blockly.Blocks[card]
    })

    Blockly.Workspace.getAll().forEach(aWorkspace => {
      aWorkspace.getAllBlocks(true).forEach(block => {
        if (block.type.startsWith('WorkspaceCard') || block.type.startsWith('WorkspaceHeroClass')) {
          let test = Blockly.Blocks
          if (Blockly.Blocks.hasOwnProperty(block.type)) {
            refreshBlock(block)
          } else {
            block.dispose(true)
          }
        }
      })
    })



    return anythingChanged
  }

  const refreshBlock = (block) => {
    block.data = Blockly.Blocks[block.type].data
    block.setFieldValue(Blockly.Blocks[block.type].message, 'message')
    block.setColour(Blockly.Blocks[block.type].json.colour)
    if (!!block.render) {
      let textElement = block.getSvgRoot().lastElementChild.firstElementChild
      if (!!Blockly.textColor && Blockly.textColor[block.getColour()]) {
        textElement.style.fill = Blockly.textColor[block.getColour()]
      } else {
        textElement.style.fill = "#fff"
      }
      block.render()
    }
  }

  const generateCard = (p) => {
    if (!p) {
      return
    }

    let cardId = null
    let card = null

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
      for (let edge of data.allJSON.edges) {
        let node = edge.node
        if (node.name === cardId) {
          card = JSON.parse(node.internal.content)
        }
      }
    }

    if (!card) {
      return
    }

    JsonConversionUtils.generateCard(mainWorkspace(), card)
  }

  const handleSearchResults = (query) => {
    if (query.length === 0) {
      setResults([])
    }
    setToolboxCategories(getToolboxCategories('Search Results'))
    if (query.length > 0) {
      mainWorkspace().getToolbox().selectFirstCategory()
      mainWorkspace().getToolbox().refreshSelection()
    } else {
      mainWorkspace().getToolbox().clearSelection()
    }
  }

  const search = (query) => {
    setResults(index
        // Query the index with search string to get an [] of IDs
        .search(query, {expand: true}) // accept partial matches
        .map(({ref}) => index.documentStore.getDoc(ref))
        .filter(doc => !props.showCatalogueBlocks ? doc.nodeType === 'Block' : (doc.nodeType === 'Card'
          && Blockly.heroClassColors.hasOwnProperty(doc.heroClass) && doc.hasOwnProperty('baseManaCost')))
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
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.cardEditor}
      toolboxCategories={toolboxCategories}
      workspaceConfiguration={
        {
          disable: false,
          zoom: {
            controls: true,
            minScale: .5,
            maxScale: 2.0,
            pinch: true
          },
          move: {
            wheel: true
          },
          renderer: props.renderer || 'spellsource'
        }
      }
      ref={blocklyEditor}
    />
   </span>
  )
})

export default CardEditorWorkspace