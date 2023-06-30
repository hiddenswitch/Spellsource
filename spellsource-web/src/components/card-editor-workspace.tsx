import React, { forwardRef, MutableRefObject, useContext, useEffect, useState } from "react";
import * as WorkspaceUtils from "../lib/workspace-utils";
import * as styles from "./card-editor-view.module.scss";
import Blockly, { BlockSvg, Toolbox, ToolboxCategory, WorkspaceSvg } from "blockly";
import { useIndex } from "../hooks/use-index";
import * as JsonConversionUtils from "../lib/json-conversion-utils";
import * as BlocklyMiscUtils from "../lib/blockly-misc-utils";
import { refreshBlock } from "../lib/blockly-misc-utils";
import SpellsourceRenderer from "../lib/spellsource-renderer";
import * as SpellsourceGenerator from "../lib/spellsource-generator";
import SimpleReactBlockly, { SimpleReactBlocklyRef } from "./simple-react-blockly";
import * as BlocklyToolbox from "../lib/blockly-toolbox";
import { cardsCategory, classesCategory, myCardsCategory, myCardsForSetCategory } from "../lib/blockly-toolbox";
import { BlocklyDataContext } from "../pages/card-editor";
import useComponentDidMount from "../hooks/use-component-did-mount";
import {
  useDeleteCardMutation,
  useGetCardLazyQuery,
  usePublishCardMutation,
  useSaveCardMutation,
} from "../__generated__/client";
import { CardDef } from "./card-display";
import { useSession } from "next-auth/react";
import { useDebounce } from "react-use";
import useBreakpoint from "@restart/hooks/useBreakpoint";
import { useEffectOnce } from "../hooks/use-effect-once";
import { uniqBy } from "lodash";
import { openFromFile } from "../lib/blockly-context-menu";
import { javascriptGenerator } from "blockly/javascript";

interface CardEditorWorkspaceProps {
  setJSON?: React.Dispatch<React.SetStateAction<string>>;
  setJS?: React.Dispatch<React.SetStateAction<string>>;
  searchCatalogueBlocks?: boolean;
  searchArtBlocks?: boolean;
  query?: string;
  defaultCard?: boolean;
  renderer?: string;
}

export type InitBlockOptions = {
  onSave: (block: BlockSvg) => void;
  onDelete: (block: BlockSvg) => void;
  onPublish: (block: BlockSvg) => void;
};

//Managing the creation and deletion of WorkspaceCard and WorkspaceHeroClass blocks
const handleWorkspaceCards = (workspace: WorkspaceSvg, userId: string) => {
  const cardScript = {} as Record<string, CardDef>;

  // Apply card ids and cardScript to all Starter_ blocks
  // TODO don't do this every single update
  workspace
    .getTopBlocks(false)
    .filter((block) => block.type.startsWith("Starter_"))
    .forEach((block: BlockSvg) => {
      block.setFieldValue(userId + "-" + block.id, "id");
      const blockXml = Blockly.Xml.blockToDom(block, true);
      const card = (block.cardScript = WorkspaceUtils.xmlToCardScript(blockXml) as CardDef);
      cardScript[card.id] = card;

      if (card.type === "CLASS") {
        BlocklyMiscUtils.setupHeroClassColor(card);
      }

      /*if (block.rendered) {
          let ogText = block.getCommentText()
          block.setCommentText(JSON.stringify(card))

          block.commentModel.size.width = 274
          block.commentModel.size.height = 324
          // block.commentModel.pinned = true

          if (block.getCommentIcon().isVisible() && block.getCommentText() !== ogText) {
            block.getCommentIcon().updateText()
          }
        }*/
    });

  // Update visuals for Cards and Class reference blocks
  Blockly.Workspace.getAll().forEach((aWorkspace) => {
    aWorkspace
      .getAllBlocks(false)
      .filter((block) => block.type.endsWith("_REFERENCE"))
      .forEach((block) => {
        const id = block.getFieldValue("id");
        if (id in cardScript) {
          const card = cardScript[id];
          const name = BlocklyMiscUtils.cardMessage(card);
          if (block.getFieldValue("name") != name) {
            block.setFieldValue(name, "name");
          }
        }
        refreshBlock(block as BlockSvg);
      });
  });

  return cardScript;
};

const CardEditorWorkspace = forwardRef(
  (props: CardEditorWorkspaceProps, blocklyEditor: MutableRefObject<SimpleReactBlocklyRef>) => {
    const { data: session } = useSession();
    const userId = session?.token?.sub ?? "";
    const data = useContext(BlocklyDataContext);
    const [results, setResults] = useState<string[]>([]);
    const index = useIndex();

    const mainWorkspace = () => blocklyEditor.current?.workspace;
    const toolbox = () => mainWorkspace()?.getToolbox() as Toolbox;

    const [json, setJson] = useState("{}");
    const [allCardScript, setAllCardScript] = useState({} as Record<string, CardDef>);
    const [saveCard] = useSaveCardMutation();
    const [deleteCard] = useDeleteCardMutation();
    const [publishCard] = usePublishCardMutation();
    const [getCard] = useGetCardLazyQuery();

    const saveAll = async () => {
      const workspace = mainWorkspace();
      if (!userId || !workspace) {
        return;
      }

      const blocks = workspace.getTopBlocks(true).filter((block) => block.cardScript) as BlockSvg[];

      await Promise.all(uniqBy(blocks, (block) => block.getFieldValue("id")).map(onSave));
      console.log("Workspace Saved");

      await data.refreshMyCards?.();
    };

    const [_, cancelDebounce] = useDebounce(saveAll, 5000, [json]);

    const onSave = async (block: BlockSvg) => {
      const cardId = block.getFieldValue("id");
      const cardScript = block.cardScript;
      if (!cardId || !userId || !cardScript) return;

      const dom = Blockly.Xml.blockToDom(block, false) as Element;
      const comment = dom.getElementsByTagName("comment")[0];
      if (comment) {
        dom.removeChild(comment);
      }

      for (const nextBlock of dom.getElementsByTagName("block")) {
        for (const childNode of nextBlock.childNodes) {
          const tagName = (childNode as Element).tagName;
          if (tagName !== "data" && tagName !== "field" && tagName !== "next") {
            nextBlock.setAttribute("collapsed", "true");
            break;
          }
        }
      }
      dom.setAttribute("collapsed", "false");

      const blocklyWorkspace = Blockly.Xml.domToText(dom);

      const isPrivate = !cardScript.public;
      if (!isPrivate) {
        delete cardScript.public;
      }

      await saveCard({
        variables: {
          cardId,
          cardScript,
          blocklyWorkspace,
        },
      });
    };

    const onDelete = async (block: BlockSvg) => {
      const cardId = block.getFieldValue("id");
      block.dispose(true, true);

      for (let blockSvg of mainWorkspace()
        .getTopBlocks(true)
        .filter((block) => block.getFieldValue("id") === cardId)) {
        blockSvg.dispose(true, true);
      }

      if (cardId) {
        await deleteCard({ variables: { cardId } });
        await data.refreshMyCards?.();
      }
    };

    const onPublish = async (block: BlockSvg) => {
      const cardId = block.getFieldValue("id");
      await onSave(block);
      await publishCard({ variables: { cardId } });
    };

    useEffect(() => {
      const classes = toolbox()?.getToolboxItemById("Classes") as ToolboxCategory;
      classes?.updateFlyoutContents(classesCategory(data));

      const cards = toolbox()?.getToolboxItemById("Cards") as ToolboxCategory;
      cards?.updateFlyoutContents(cardsCategory(data));

      const myCards = toolbox()?.getToolboxItemById("My Cards") as ToolboxCategory;
      myCards?.updateFlyoutContents(myCardsCategory(data));

      uniqBy(
        data.myCards.map((card) => card.cardScript.set),
        (card) => card.set
      ).forEach((set) => {
        if (set === "CUSTOM") return;
        const category = toolbox()?.getToolboxItemById(`My ${set} Cards`) as ToolboxCategory;
        category?.updateFlyoutContents(myCardsForSetCategory(set, data));
      });

      // TODO visually remove set category if no more blocks in it

      mainWorkspace().refreshToolboxSelection();
      onWorkspaceChanged();
    }, [data.myCards]);

    // Run once before the workspace has been created
    useComponentDidMount(() => {
      if (!Blockly["spellsourceInit"]) {
        BlocklyMiscUtils.initBlocks(data, { onSave, onDelete, onPublish });
        BlocklyMiscUtils.initHeroClassColors(data);
        BlocklyMiscUtils.initArtBlcks(data);
        Blockly.blockRendering.register("spellsource", SpellsourceRenderer);
        SpellsourceGenerator.generateJavaScript();
        Blockly["spellsourceInit"] = true;
      }
    });

    // Run once after the workspace has been created
    useEffectOnce(() => {
      mainWorkspace().getTheme().setStartHats(true);

      BlocklyToolbox.initCallbacks(mainWorkspace());

      let params = new URLSearchParams(window.location.search);
      let card = params.get("card");
      if (!!card) {
        generateCard(card);
      }

      // Let you ctrl S to save
      const listener = (evt) => {
        if (evt.key === "s" && (evt.metaKey || evt.ctrlKey)) {
          evt.preventDefault();
          cancelDebounce(); // Immediately run saveAll, skipping next one
          saveAll();
        }

        if (evt.key === "o" && (evt.metaKey || evt.ctrlKey)) {
          evt.preventDefault();
          openFromFile.callback({ workspace: mainWorkspace(), block: undefined });
        }
      };
      document.addEventListener("keydown", listener);
      return () => document.removeEventListener("keydown", listener);
    });

    //When the query is updated, do some searching
    useEffect(() => {
      search(props.query);
      handleSearchResults(props.query);
    }, [props.query]);

    //Switch the renderer
    useEffect(() => {
      BlocklyMiscUtils.switchRenderer(props.renderer, mainWorkspace());
    }, [props.renderer]);

    //The default workspace changed event handler
    const onWorkspaceChanged = () => {
      if (mainWorkspace().isDragging()) return; // While dragging a block, it doesn't appear in the blocks list

      const cardScript = handleWorkspaceCards(mainWorkspace(), data.userId || "guest");
      BlocklyMiscUtils.pluralStuff(mainWorkspace());
      let json = JSON.stringify(cardScript, null, 2);
      props.setJSON(json);
      setJson(json);
      setAllCardScript(cardScript);
      props.setJS(javascriptGenerator.workspaceToCode(mainWorkspace()));
    };

    const generateCard = (p: string) => {
      if (!p) {
        return;
      }

      let cardId = null;
      let card = null;

      if (p.includes("{")) {
        card = JSON.parse(p);
      } else if (p.includes("www")) {
        cardId = p.split("cards/")[1];
      } else if (p.includes("_") || p.includes("-")) {
        cardId = p;
      }

      if (cardId && !card) {
        if (cardId in data.classes) {
          card = data.classes[cardId];
        } else {
          getCard({ variables: { id: cardId } }).then((value) => {
            const card = value.data?.getLatestCard;
            if (card) {
              if (card.blocklyWorkspace) {
                const dom = Blockly.utils.xml.textToDom(card.blocklyWorkspace);
                const block = Blockly.Xml.domToBlock(dom, mainWorkspace());
              } else if (card.cardScript) {
                JsonConversionUtils.generateCard(mainWorkspace(), card.cardScript);
              }
            }
          });
        }
      }

      if (card) {
        JsonConversionUtils.generateCard(mainWorkspace(), card);
      }
    };

    const handleSearchResults = (query) => {
      if (query.length === 0) {
        setResults([]);
      }

      (toolbox()?.getToolboxItemById("Search Results") as ToolboxCategory).updateFlyoutContents(
        BlocklyToolbox.searchResultsCategory(results)
      );
      toolbox()?.clearSelection();
      if (query.length > 0) {
        toolbox().selectItemByPosition(0);
        toolbox().refreshSelection();
      }
    };

    const search = (query: string) => {
      if (!index) return;

      setResults(
        index
          // Query the index with search string to get an [] of IDs
          .search(query, { expand: true }) // accept partial matches
          .map(({ ref }) => index.documentStore.getDoc(ref))
          .filter((doc) => {
            if (props.searchCatalogueBlocks) {
              return (
                doc.nodeType === "Card" &&
                "baseManaCost" in doc.node &&
                doc.node.heroClass in Blockly["heroClassColors"]
              );
            }
            if (props.searchArtBlocks) {
              return doc.nodeType === "File" && !!Blockly.Blocks["Art_" + doc.title];
            }
            return doc.nodeType === "Block";
          })
          .map((doc) => {
            if (doc.nodeType === "Card") {
              return "CatalogueCard_" + doc.id;
            }
            if (doc.nodeType === "File") {
              return "Art_" + doc.title;
            }
            return doc.id;
          })
          .slice(0, 20)
      );
    };

    const xs = !useBreakpoint("sm", "up");

    return (
      <SimpleReactBlockly
        workspaceDidChange={onWorkspaceChanged}
        wrapperDivClassName={styles.cardEditor as string}
        workspaceConfiguration={{
          disable: false,
          zoom: {
            controls: true,
            minScale: 0.5,
            maxScale: 2.0,
            pinch: true,
            startScale: xs ? 0.75 : 1,
          },
          move: {
            wheel: true,
          },
          oneBasedIndex: false,
          horizontalLayout: xs,
          renderer: props.renderer || "spellsource",
          toolbox: BlocklyToolbox.editorToolbox(results, data),
        }}
        ref={blocklyEditor}
      />
    );
  }
);

export default CardEditorWorkspace;
