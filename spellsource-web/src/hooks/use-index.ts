import {useContext, useRef} from 'react'

import elasticlunr, {Index} from 'elasticlunr'
import {BlocklyDataContext} from "../pages/card-editor";
import {BlockDef} from "../lib/blocks";
import {CardDef} from "../components/card-display";
import {ImageDef} from "../__generated__/client";

export type SearchNode = BlockSearchNode | CardSearchNode | ArtSearchNode | MarkdownSearchNode
interface ISearchNode {
  id: string
  title: string
  description: string
  path: string
}

interface BlockSearchNode extends ISearchNode {
  nodeType: "Block"
  node: BlockDef
}

interface CardSearchNode extends ISearchNode {
  nodeType: "Card"
  node: CardDef
}

interface ArtSearchNode extends ISearchNode {
  nodeType: "File"
  node: ImageDef
}

interface MarkdownSearchNode extends ISearchNode {
  nodeType: "MarkdownRemark"
  node: string
}

export const cardSearchNode = (card: CardDef): CardSearchNode => ({
  id: card.id,
  title: card.name || "",
  description: card.description || "",
  nodeType: "Card",
  node: card,
  path: `/cards/${card.id}`
});

// returns index
export const useIndex = () => {
  const {allBlocks, allArt, cardsById, blocksByType, ready} = useContext(BlocklyDataContext);
  const index = useRef<Index<SearchNode> | undefined>(undefined)
  if (!index.current && ready) {
    index.current = elasticlunr<SearchNode>(idx => {
      idx.setRef("id");
      idx.addField("title");
      idx.addField("description")

      for (const block of allBlocks) {
        idx.addDoc({
          id: block.type ,
          title: setupSearchMessage(block, blocksByType) ?? "",
          description: block.comment || "",
          nodeType: "Block",
          node: block,
          path: `/card-editor?block=${block.type}`
        })
      }

      for (const art of allArt) {
        idx.addDoc({
          id: art.id,
          title: art.name,
          description: art.src,
          nodeType: "File",
          node: art,
          path: art.src
        })
      }

      for (const card of Object.values(cardsById)) {
        idx.addDoc(cardSearchNode(card))
      }
    });
  }

  return index.current
}

const setupSearchMessage = (block: BlockDef, blocksByType: Record<string, BlockDef>) => {
  const getTextForBlock = (node: BlockDef) => {
    let text = ''
    if (node.messages) {
      for (let i = 0; i < node.messages.length; i++) {
        let message = node.messages[i]
        if (!!node.args && !!node.args[i] && !!node.args[i].args) {
          let args = node.args[i].args
          for (let j = 0; j < args.length; j++) {
            let text = getTextForArg(args[j])
            message = message.replace('%' + (j + 1).toString(), text)
          }
        }
        text += message + ' '
      }
    }
    return text
  }

  const getTextForArg = (arg) => {
    if (arg.shadow?.type && arg.shadow.type in blocksByType) {
      return getTextForBlock(blocksByType[arg.shadow.type])
    }
    if (!!arg.options) {
      let text = ''
      for (let option of arg.options) {
        text += option[0] + ' '
      }
      return text
    }
    if (arg.type === 'field_label_plural') {
      return arg.value
    }
    return ''
  }

  return getTextForBlock(block).replace(/\s+/g, ' ').trim()
}


/*
export const useBlockIndex = () => {
  const {allBlocks, blocksByType} = useContext(BlocklyDataContext);
  const index = useRef<Index<BlockDef> | undefined>(undefined)
  if (!index.current && allBlocks) {
    index.current = elasticlunr<BlockDef>(idx => {
      idx.setRef("type");
      idx.addField("comment")
      idx.addField("searchMessage")

      for (const block of allBlocks) {
        setupSearchMessage(block, blocksByType)
        idx.addDoc(block)
      }
    });
  }

  return index.current
}

*/
