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
  }, [])

  useEffect(() => {
    search(props.query)
    handleSearchResults(props.query)
  }, [props.query])

  useEffect(() => {
    BlocklyMiscUtils.switchRenderer(props.renderer, mainWorkspace())
  }, [props.renderer])

  const getToolboxCategories = (onlyCategory = null) => {
    let index = -1
    return data.editorToolbox.BlockCategoryList.map((
      {
        BlockTypePrefix, CategoryName, ColorHex, Subcategories, Tooltip, Subtooltips
      }) => {
      if (!!Blockly.categoryTooltips && !Blockly.categoryTooltips[CategoryName]) {
        Blockly.categoryTooltips[CategoryName] = Tooltip
        Blockly.tooltipColors[CategoryName] = BlocklyMiscUtils.tertiaryColor(BlocklyMiscUtils.colorToHex(ColorHex))
      }
      index++
      if (!!onlyCategory && CategoryName !== onlyCategory) {
        return toolboxCategories[index] //my attempt to reduce the runtime a bit
      }
      let blocks = []
      if (!!BlockTypePrefix) {
        for (let blocksKey of BlocklyMiscUtils.allEditorBlocks()) {
          if ((!blocksKey.endsWith('SHADOW') && blocksKey.startsWith(BlockTypePrefix))
            || (CategoryName === 'Cards' && blocksKey.startsWith('WorkspaceCard_'))
            || (CategoryName === 'Classes' && blocksKey.startsWith('WorkspaceHeroClass_'))) {
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

      if (CategoryName === 'Classes') {
        blocks = blocks.sort((a, b) => {
          if (a.type.startsWith('WorkspaceHeroClass')) {
            return -1
          }
          if (b.type.startsWith('WorkspaceHeroClass')) {
            return 1
          }
          return 0
        })
      }

      if (!!Subcategories && isArray(Subcategories)) {
        let categories = []


        for (let i = 0, category; (category = Subcategories[i]); i++) {
          categories[category] = {
            name: category,
            blocks: [],
            colour: ColorHex
          }
          if (!!Subtooltips && !!Subtooltips[i]) {
            let name = CategoryName + '.' + category
            if (!!Blockly.categoryTooltips && !Blockly.categoryTooltips[name]) {
              Blockly.categoryTooltips[name] = Subtooltips[i]
              Blockly.tooltipColors[name] = BlocklyMiscUtils.tertiaryColor(BlocklyMiscUtils.colorToHex(ColorHex))
            }
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
        colour: ColorHex
      }
    })
  }

  //Turns our own json formatting for shadow blocks into the formatting
  //that's used for specifying toolbox categories (recursively)
  const shadowBlockJsonCreation = (type) => {
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
    for (let blocksKey of BlocklyMiscUtils.allEditorBlocks()) {
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