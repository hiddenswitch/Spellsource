import React, { useEffect, useMemo, useState } from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import { isArray } from 'lodash'
import AceEditor from 'react-ace'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import { Form } from 'react-bootstrap'
import { useIndex } from '../hooks/use-index'
import JsonConversionUtils from '../lib/json-conversion-utils'
import BlocklyMiscUtils from '../lib/blockly-misc-utils'
import useComponentWillMount from '../hooks/use-component-will-mount'
import useBlocklyData from '../hooks/use-blockly-data'

const CardEditorView = () => {
  const data = useBlocklyData()

  const heroClassColors = useMemo(() => BlocklyMiscUtils.getHeroClassColors(data))
  const [code, setCode] = useState(``)
  const [query, setQuery] = useState(``)
  const [checked, setChecked] = useState(false)
  const [results, setResults] = useState([])
  const index = useIndex()

  // Run once before the workspace has been created
  useComponentWillMount(() => {
    BlocklyMiscUtils.initializeBlocks(data)
  })

  // Run once after the workspace has been created
  useEffect(() => {
    const workspace = Blockly.getMainWorkspace()
    const importCardCallback = () => {
      generateCard()
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
    return () => {
      workspace.removeButtonCallback('importCard')
      workspace.removeChangeListener(changeListenerCallback)
    }
  }, [])

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
            || (CategoryName === 'Cards' && blocksKey.startsWith('WorkspaceCard'))) {
            blocks.push({
              type: blocksKey,
              values: shadowBlockJsonCreation(blocksKey),
              next: blocksKey.startsWith('Starter') ?
                { type: 'Property_SHADOW', shadow: true }
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
            next: value.id.startsWith('Starter') ?
              { type: 'Property_SHADOW', shadow: true }
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
  function shadowBlockJsonCreation (type) {
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

  function onWorkspaceChanged (workspace) {
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

  function createCard (card, workspace, cardsStillInUse) {
    if (!!card && !!card.name) {
      let cardId = card.type.toLowerCase()
        + '_'
        + card.name.toLowerCase().replace(' ', '_')
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
      cardsStillInUse.push(type)
      if (Blockly.Blocks[type] !== block) {
        Blockly.Blocks[type] = block
        return true
      }
      return false
    }

  }

  function generateCard () {
    let p = prompt('Input the name of the card (or the wiki page URL / Card ID for more precision)')
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
                    style={{ width: '50%' }}
      />
      <Form.Check style={{ display: 'inline' }}>
        <Form.Check.Input onChange={e => {
          setChecked(!checked)
          if (query.length > 0) {
            search({ target: { value: query } })
            handleSearchResults({ target: { value: query } })
          }
        }}
                          value={checked}
                          style={
                            {
                              height: '15px',
                              width: '15px',
                              webkitAppearance: 'checkbox'
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