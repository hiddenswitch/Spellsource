import React, {useEffect, useMemo, useState} from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import {isArray, uniq} from 'lodash'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import {useIndex} from '../hooks/use-index'
import JsonConversionUtils from '../lib/json-conversion-utils'
import BlocklyMiscUtils from '../lib/blockly-misc-utils'
import useComponentWillMount from '../hooks/use-component-will-mount'
import useBlocklyData from '../hooks/use-blockly-data'

const CardEditorWorkspace = (props) => {
  const data = useBlocklyData()
  const heroClassColors = useMemo(() => BlocklyMiscUtils.getHeroClassColors(data))
  const [query, setQuery] = useState(``)
  const [results, setResults] = useState([])
  const defaultCard = props.defaultCard
  const index = useIndex()

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


    workspace.addChangeListener((event) => pluralStuff(event, workspace))

    return () => {
      workspace.removeButtonCallback('importCard')
      workspace.removeChangeListener(changeListenerCallback)
      workspace.removeChangeListener(pluralStuff)
    }
  }, [])

  const pluralStuff = (event, workspace) => {
    let anyChange = false

    if (event.type !== Blockly.Events.UI && !workspace.isDragging()) {
      for (let block of workspace.getAllBlocks()) {
        let argsList = JsonConversionUtils.argsList(block.json);
        for (let arg of argsList) {
          if (arg.type === 'field_label_plural') {
            let shouldBePlural = null
            let connection
            //on a plural field, 'src' is where it should look to to know whether it's plural or not
            if (arg.src === 'OUTPUT') {
              connection = block.outputConnection
              if (!!block.outputConnection.targetBlock()) {
                let targetBlock = connection.targetBlock()
                if (targetBlock.type.endsWith('_I') && !!targetBlock.getPreviousBlock()) {
                  let prevBlock = targetBlock.getPreviousBlock()
                  while (!!prevBlock.getPreviousBlock()) {
                    prevBlock = prevBlock.getPreviousBlock()
                  }
                  connection = prevBlock.outputConnection
                  targetBlock = connection.targetBlock()
                }
                if (!!targetBlock) { //if the 'src' arg appears on the 'input_value' it's connected to, redirect to that
                  let name = targetBlock.getInputWithBlock(block)?.name
                  for (let arg of JsonConversionUtils.inputsList(targetBlock.json)) {
                    if (arg.name === name && !!arg.src) { //
                      connection = targetBlock.getInput(arg.src).connection
                    }
                  }
                }
              }
            } else {
              connection = block.getInput(arg.src)?.connection
            }
            if (!connection) {
              if (!!block.getField(arg.src)) {
                shouldBePlural = block.getFieldValue(arg.src) !== 1
              }
            } else if (!!connection.targetBlock()) {
              let targetBlock = connection.targetBlock()
              if (targetBlock.json?.plural != null) {
                shouldBePlural = targetBlock.json.plural
              } else if (targetBlock.type === 'ValueProvider_int') {
                shouldBePlural = targetBlock.getFieldValue('int') !== 1
              }
            }

            let before = block.getFieldValue(arg.name)
            const options = arg.text.split('/')
            if (shouldBePlural === null) {
              if (arg.value) { //on a plural field, 'value' is the default text to show (e.g. in the toolbox)
                block.setFieldValue(arg.value, arg.name)
              } else {
                block.setFieldValue(arg.text, arg.name)
              }
            } else if (shouldBePlural) {
              block.setFieldValue(options[1], arg.name)
            } else {
              block.setFieldValue(options[0], arg.name)
            }

            if (block.getFieldValue(arg.name) !== before) {
              anyChange = true
            }
          }
        }
      }

      if (anyChange) {

        for (let block of workspace.getAllBlocks()) {
          for (var i = 0, input; (input = block.inputList[i]); i++) {
            for (var j = 0, field; (field = input.fieldRow[j]); j++) {
              if (field.isBeingEdited_ && field.showEditor_) {
                field.showEditor_()
              }
            }
          }
        }
      }
    }
  }

  const getToolboxCategories = (onlyCategory = null) => {
    let index = -1
    return data.toolbox.BlockCategoryList.map((
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
    if (update) {
      setToolboxCategories(getToolboxCategories('Cards'))
    }
  }

  const createCard = (card, workspace, cardsStillInUse) => {
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
      let type = 'WorkspaceCard_' + cardId
      let color = '#888888'
      if (!!card.heroClass) {
        color = heroClassColors[card.heroClass]
      }
      let json = {
        'type': type,
        'message0': BlocklyMiscUtils.cardMessage(card),
        'output': 'Card',
        'colour': color
      }
      let block = {
        init: function () {
          this.jsonInit(json)
        },
        data: cardId,
        json: json
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

    JsonConversionUtils.generateCard(Blockly.getMainWorkspace(), card)
  }

  const handleSearchResults = (query) => {
    if (query.length === 0) {
      setResults([])
    }
    setToolboxCategories(getToolboxCategories('Search Results'))
    const workspace = Blockly.getMainWorkspace()
    if (query.length > 0) {
      workspace.getToolbox().selectFirstCategory()
      workspace.getToolbox().refreshSelection()
    } else {
      workspace.getToolbox().clearSelection()
    }
  }

  const search = (query) => {
    setResults(index
        // Query the index with search string to get an [] of IDs
        .search(query, {expand: true}) // accept partial matches
        .map(({ref}) => index.documentStore.getDoc(ref))
        .filter(doc => !props.showCatalogueBlocks ? doc.nodeType === 'Block' : (doc.nodeType === 'Card'
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

  //when this component reloads, check if the search query changed
  if (props.query !== query) {
    setQuery(props.query)
    search(props.query)
    handleSearchResults(props.query)
  }

  return (<span>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.codeEditor}
      toolboxCategories={toolboxCategories}
    />
   </span>
  )
}

export default CardEditorWorkspace