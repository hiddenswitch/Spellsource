import React, {forwardRef, MutableRefObject, useContext, useEffect, useState} from 'react'
import * as WorkspaceUtils from '../lib/workspace-utils'
import * as styles from './card-editor-view.module.scss'
import Blockly, {BlockSvg, Toolbox, ToolboxCategory, WorkspaceSvg} from 'blockly'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import {useIndex} from '../hooks/use-index'
import * as JsonConversionUtils from '../lib/json-conversion-utils'
import * as BlocklyMiscUtils from '../lib/blockly-misc-utils'
import SpellsourceRenderer from '../lib/spellsource-renderer'
import * as SpellsourceGenerator from '../lib/spellsource-generator'
import SimpleReactBlockly from './simple-react-blockly'
import * as BlocklyToolbox from '../lib/blockly-toolbox'
import {BlocklyDataContext} from "../pages/card-editor";
import useComponentDidMount from '../hooks/use-component-did-mount'
import {useDeleteCardMutation, useGetCardLazyQuery, useUpsertCardMutation} from "../__generated__/client";
import {CardDef} from './card-display'
import {useSession} from "next-auth/react";
import {useDebounce} from "react-use";
import uniqueBy from "@popperjs/core/lib/utils/uniqueBy";

interface CardEditorWorkspaceProps {
  setJSON?: React.Dispatch<React.SetStateAction<string>>
  setJS?: React.Dispatch<React.SetStateAction<string>>
  searchCatalogueBlocks?: boolean;
  searchArtBlocks?: boolean;
  query?: string;
  defaultCard?: boolean;
  renderer?: string;
}

export type InitBlockOptions = {
  onSave: (block: BlockSvg) => void
  onDelete: (block: BlockSvg) => void
}


//Create a new WorkspaceHeroClass block
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
      Blockly.textColor[blockType] = Blockly.utils.colour.rgbToHex(
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

//Create a new WorkspaceCard block
const createCard = (card: CardDef, blockType) => {
  if (!!card && !!card.name && !!card.type) {
    let cardId = card.id;

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

const createWorkspaceCard = (card: CardDef) => {
  let type;
  let created;

  if (card.type === "CLASS") {
    type = 'WorkspaceHeroClass_' + card.id;
    created = createClass(card, type)
  } else {
    type = 'WorkspaceCard_' + card.id;
    created = createCard(card, type)
  }

  const changed = !(type in Blockly.Blocks);
  Blockly.Blocks[type] = created
  Blockly.JavaScript[type] = () => '\'' + created.data + '\''
  return {type, changed};
}

const CardEditorWorkspace = forwardRef((props: CardEditorWorkspaceProps, blocklyEditor: MutableRefObject<SimpleReactBlockly>) => {
  const {data: session} = useSession();
  const userId = session?.token?.sub ?? "";
  const data = useContext(BlocklyDataContext)
  const [results, setResults] = useState<string[]>([])
  const index = useIndex()

  const mainWorkspace = () => blocklyEditor.current?.workspace
  const toolbox = () => mainWorkspace()?.getToolbox() as Toolbox;

  const [allCardScript, setAllCardScript] = useState("");
  const [upsertCard] = useUpsertCardMutation();
  const [deleteCard] = useDeleteCardMutation();
  const [getCard] = useGetCardLazyQuery()

  const saveAll = async () => {
    const workspace = mainWorkspace();
    if (!userId || !workspace) {
      return;
    }

    const blocks = workspace.getTopBlocks(true).filter(block => block.cardScript) as BlockSvg[];

    await Promise.all(uniqueBy(blocks, block => block.getFieldValue("id")).map(onSave));
    console.log("Workspace Saved");
  }

  useDebounce(saveAll, 5000, [allCardScript])

  const onSave = async (block: BlockSvg) => {
    const cardId = block.getFieldValue("id");
    const cardScript = block.cardScript;
    if (!cardId || !userId || !cardScript) return;
    const dom = Blockly.Xml.blockToDom(block, true);
    const blocklyWorkspace = Blockly.Xml.domToText(dom);

    await upsertCard({variables: {cardId, card: {id: cardId, createdBy: userId, cardScript, blocklyWorkspace}}});
  }

  const onDelete = async (block: BlockSvg) => {
    const cardId = block.getFieldValue("id");
    block.dispose(true, true)
    if (cardId) {
      await deleteCard({variables: {cardId}})
    }
  }

  // Run once before the workspace has been created
  useComponentDidMount(() => {
    if (!Blockly.spellsourceInit) {
      BlocklyMiscUtils.initBlocks(data, {onSave, onDelete})
      BlocklyMiscUtils.initHeroClassColors(data)
      BlocklyMiscUtils.initArtBlcks(data)
      BlocklyMiscUtils.initCardBlocks(data)
      Blockly.blockRendering.register('spellsource', SpellsourceRenderer)
      SpellsourceGenerator.generateJavaScript()
      Blockly.spellsourceInit = true
    }
  })

  // Run once after the workspace has been created
  useEffect(() => {
    if (props.defaultCard) {
      setTimeout(() => {
        const array = ['Daring Duelist', 'Ninja Aspirants', 'Redhide Butcher',
          'Sly Conquistador', 'Stormcloud Assailant', 'Peacock Mystic']
        generateCard(array[Math.floor(Math.random() * array.length)])
        //mainWorkspace.getTopBlocks(true)[0].setCommentText('This card was imported automatically as an example.')
      }, 100)
    }

    mainWorkspace().getTheme().setStartHats(true)

    BlocklyToolbox.initCallbacks(mainWorkspace())

    let params = new URLSearchParams(window.location.search)
    let card = params.get('card')
    if (!!card) {
      generateCard(card)
    }


    for (const myCard of data.myCards) {
      createWorkspaceCard(myCard.cardScript);
    }

    for (const myCard of data.myCards) {
      const dom = Blockly.Xml.textToDom(myCard.blocklyWorkspace);
      dom.getElementsByTagName("comment")[0]?.setAttribute("pinned", "false");
      Blockly.Xml.domToBlock(dom, mainWorkspace());
    }

    const listener = (evt,) => {
      if (evt.key === "s" && (evt.metaKey || evt.ctrlKey)) {
        evt.preventDefault();
        saveAll();
      }
    }
    document.addEventListener("keydown", listener)
    return () => document.removeEventListener("keydown", listener)
  }, [])

  //When the query is updated, do some searching
  useEffect(() => {
    search(props.query)
    handleSearchResults(props.query)
  }, [props.query])

  //Switch the renderer
  useEffect(() => {
    BlocklyMiscUtils.switchRenderer(props.renderer, mainWorkspace())
  }, [props.renderer])

  //The default workspace changed event handler
  const onWorkspaceChanged = () => {
    const cardScript = [] as CardDef[]
    //WorkspaceUtils.workspaceToCardScript(mainWorkspace)
    // Generate the blocks that correspond to the cards in the workspace
    if (!mainWorkspace().isDragging()) {
      let update = handleWorkspaceCards(mainWorkspace(), cardScript)
      if (update) {
        toolbox().getToolboxItemById<ToolboxCategory>('Cards').updateFlyoutContents(BlocklyToolbox.cardsCategory())
        toolbox().getToolboxItemById<ToolboxCategory>('Classes').updateFlyoutContents(BlocklyToolbox.classesCategory())
      }

      BlocklyMiscUtils.pluralStuff(mainWorkspace())

      let json = JSON.stringify(cardScript, null, 2);
      props.setJSON(json)
      setAllCardScript(json);
    }

    props.setJS(Blockly.JavaScript.workspaceToCode(mainWorkspace()))
  }


  //Managing the creation and deletion of WorkspaceCard and WorkspaceHeroClass blocks
  const handleWorkspaceCards = (workspace: WorkspaceSvg, cardScript: CardDef[]) => {
    let anythingChanged = false

    let remainingCards = new Set(Object.keys(Blockly.Blocks)
      .filter(blocksKey => blocksKey.startsWith('WorkspaceCard') || blocksKey.startsWith('WorkspaceHeroClass')));

    workspace.getTopBlocks(true)
      .filter(block => block.type.startsWith('Starter_'))
      .forEach((block: BlockSvg) => {
          if (!block.getFieldValue("id")) {
            block.setFieldValue(crypto.randomUUID(), "id");
          }
          const blockXml = Blockly.Xml.blockToDom(block, true)
          const card = block.cardScript = WorkspaceUtils.xmlToCardScript(blockXml) as CardDef
          cardScript.push(card)

          //if it's a class card, make the class first to init the color
          const {type, changed} = createWorkspaceCard(card);
          remainingCards.delete(type)
          anythingChanged ||= changed;

          if (block.rendered) {
            let ogText = block.getCommentText()
            block.setCommentText(JSON.stringify(card))

            block.commentModel.size.width = 274
            block.commentModel.size.height = 324
            block.commentModel.pinned = true

            if (block.getCommentIcon().isVisible() && block.getCommentText() !== ogText) {
              block.getCommentIcon().updateText()
            }
          }
        }
      )

    remainingCards.forEach(card => {
      anythingChanged = true
      delete Blockly.Blocks[card]
    })

    Blockly.Workspace.getAll().forEach(aWorkspace => {
      aWorkspace.getAllBlocks(true).forEach(block => {
        if (block.type.startsWith('WorkspaceCard') || block.type.startsWith('WorkspaceHeroClass')) {
          if (block.type in Blockly.Blocks) {
            BlocklyMiscUtils.refreshBlock(block as BlockSvg)
          } else {
            block.dispose(true)
          }
        }
      })
    })

    return anythingChanged
  }


  const generateCard = (p: string) => {
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
      for (let node of Object.values(data.cardsById)) {
        if (node.name.toLowerCase() === p.toLowerCase()) {
          cardId = node.id
          card = node;
          break
        }
      }
    }

    if (cardId && !card) {
      if (cardId in data.cardsById) {
        card = data.cardsById[cardId];
      } else {
        getCard({variables: {id: cardId}}).then(value => {
          const card = value.data?.cardById;
          if (card) {
            if (card.blocklyWorkspace) {

            } else if (card.cardScript) {
              JsonConversionUtils.generateCard(mainWorkspace(), card.cardScript)
            }
          }
        })
      }
    }

    if (card) {
      JsonConversionUtils.generateCard(mainWorkspace(), card)
    }
  }

  const handleSearchResults = (query) => {
    if (query.length === 0) {
      setResults([])
    }
    toolbox()?.getToolboxItemById<ToolboxCategory>('Search Results')
      .updateFlyoutContents(BlocklyToolbox.searchResultsCategory(results))
    toolbox()?.clearSelection()
    if (query.length > 0) {
      toolbox().selectItemByPosition(0)
      toolbox().refreshSelection()
    }
  }

  const search = (query: string) => {
    if (!index) return;

    setResults(index
        // Query the index with search string to get an [] of IDs
        .search(query, {expand: true}) // accept partial matches
        .map(({ref}) => index.documentStore.getDoc(ref))
        .filter(doc => {
          if (props.searchCatalogueBlocks) {
            return doc.nodeType === 'Card' && "baseManaCost" in doc.node && doc.node.heroClass in Blockly.heroClassColors
          }
          if (props.searchArtBlocks) {
            return doc.nodeType === 'File' && !!Blockly.Blocks['Art_' + doc.title]
          }
          return doc.nodeType === 'Block'
        })
        .map(doc => {
          if (doc.nodeType === 'Card') {
            return 'CatalogueCard_' + doc.id
          }
          if (doc.nodeType === 'File') {
            return 'Art_' + doc.title
          }
          return doc.id
        })
        .slice(0, 20)
      // map over each ID and return full document
    )
  }

  return <SimpleReactBlockly
    workspaceDidChange={onWorkspaceChanged}
    wrapperDivClassName={styles.cardEditor as string}
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
        renderer: props.renderer || 'spellsource',
        toolbox: BlocklyToolbox.editorToolbox(results)
      }
    }
    ref={blocklyEditor}
  />
})

export default CardEditorWorkspace
