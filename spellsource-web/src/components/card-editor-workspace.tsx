import React, { forwardRef, useContext, useEffect, useMemo, useState } from "react";
import * as WorkspaceUtils from "../lib/workspace-utils";
import Blockly, {
  Block,
  BlockSvg,
  ISelectableToolboxItem,
  IToolboxItem,
  Toolbox,
  ToolboxCategory,
  WorkspaceSvg,
} from "blockly";
import * as JsonConversionUtils from "../lib/json-conversion-utils";
import * as BlocklyMiscUtils from "../lib/blockly-spellsource-utils";
import { refreshBlock } from "../lib/blockly-spellsource-utils";
import * as SpellsourceGenerator from "../lib/spellsource-generator";
import SimpleReactBlockly, { SimpleReactBlocklyRef } from "./simple-react-blockly";
import * as BlocklyToolbox from "../lib/blockly-toolbox";
import {
  cardsCategory,
  classesCategory,
  generatedArtCategory,
  myCardsCategory,
  myCardsForSetCategory,
} from "../lib/blockly-toolbox";
import { BlocklyDataContext } from "../pages/card-editor";
import useComponentDidMount from "../hooks/use-component-did-mount";
import {
  useDeleteArtMutation,
  useDeleteCardMutation,
  useGetCardLazyQuery,
  usePublishCardMutation,
  useSaveCardMutation,
} from "../__generated__/client";
import { CardDef } from "./collection/card-display";
import { useSession } from "next-auth/react";
import { useDebounce, useDrop } from "react-use";
import useBreakpoint from "@restart/hooks/useBreakpoint";
import { useEffectOnce } from "../hooks/use-effect-once";
import { uniq, uniqBy } from "lodash";
import { openFromFile } from "../lib/blockly-context-menu";
import cx from "classnames";
import * as BlocklyRegister from "../lib/blockly-setup";
import { initWorkspace, plugins } from "../lib/blockly-setup";
import * as BlocklyModification from "../lib/blockly-patches";
import SpellsourceRenderer from "../lib/spellsource-renderer";
import { CardDesc } from "../__generated__/spellsource-game";
import { newBlock } from "../lib/blockly-utils";
import { compareNodes } from "@graphql-tools/utils";

interface CardEditorWorkspaceProps {
  setJSON?: React.Dispatch<React.SetStateAction<string>>;
  setJS?: React.Dispatch<React.SetStateAction<string>>;
  searchCatalogueBlocks?: boolean;
  searchArtBlocks?: boolean;
  defaultCard?: boolean;
  renderer?: string;
}

export type InitBlockOptions = {
  onSave: (block: BlockSvg) => void;
  onDelete: (block: BlockSvg) => void;
  onPublish: (block: BlockSvg) => void;
  generateCardAsync: (cardId: string) => Promise<Block | null>;
  deleteArtAsync: (hash: string) => Promise<void>;
};

//Managing the creation and deletion of WorkspaceCard and WorkspaceHeroClass blocks
const handleWorkspaceCards = (
  workspace: WorkspaceSvg,
  userId: string,
  getCard: ReturnType<typeof useGetCardLazyQuery>[0]
) => {
  const cardScript = {} as Record<string, CardDef>;

  // Apply card ids and cardScript to all Starter_ blocks
  // TODO don't do this every single update
  workspace
    .getTopBlocks(false)
    .filter((block) => block.type.startsWith("Starter_"))
    .forEach((block: BlockSvg & { cardScript?: CardDef }) => {
      block.setFieldValue(userId + "-" + block.id, "id");

      const card = (block.cardScript = WorkspaceUtils.blockToCardScript(block) as CardDef);
      cardScript[card.id] = card;

      if (card.type === "CLASS") {
        BlocklyMiscUtils.setupHeroClassColor(card);
      }
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
        } else if (block.type === "Card_REFERENCE" && id === block.getFieldValue("name")) {
          getCard({ variables: { id } }).then((result) => {
            const card = result?.data?.getLatestCard?.cardScript;
            if (!card) return;
            const name = BlocklyMiscUtils.cardMessage(card);
            if (block.getFieldValue("name") != name) {
              block.setFieldValue(name, "name");
            }
            refreshBlock(block as BlockSvg);
          });
        }
        refreshBlock(block as BlockSvg);
      });
  });

  return cardScript;
};

const CardEditorWorkspace = forwardRef<SimpleReactBlocklyRef, CardEditorWorkspaceProps>((props, blocklyEditor) => {
  const { data: session } = useSession();
  const userId = session?.token?.sub ?? "";
  const data = useContext(BlocklyDataContext);
  const [results, setResults] = useState<string[]>([]);

  const mainWorkspace = () => {
    if (blocklyEditor !== null && "current" in blocklyEditor) {
      return blocklyEditor.current?.workspace;
    }
    throw new Error("callback refs unsupported");
  };
  const getToolbox = () => mainWorkspace()?.getToolbox() as Toolbox;

  const [json, setJson] = useState("{}");
  const [saveCard] = useSaveCardMutation();
  const [deleteCard] = useDeleteCardMutation();
  const [publishCard] = usePublishCardMutation();
  const [getCard] = useGetCardLazyQuery();

  const saveAll = async () => {
    const workspace = mainWorkspace();
    if (!userId || !workspace) {
      return;
    }

    const blocks = workspace.getTopBlocks(true).filter((block) => "cardScript" in block) as BlockSvg[];

    await Promise.all(uniqBy(blocks, (block) => block.getFieldValue("id")).map(onSave));
    console.log("Workspace Saved");

    await data.refreshMyCards?.();
  };

  const [_, cancelDebounce] = useDebounce(saveAll, 5000, [json]);

  const onSave = async (block: BlockSvg) => {
    const cardId = block.getFieldValue("id");
    const cardScript =
      "cardScript" in block
        ? (block["cardScript"] as CardDesc & {
            public?: boolean;
          })
        : null;
    if (!cardId || !userId || !cardScript) return;

    const blockState = Blockly.serialization.blocks.save(block);

    if (!blockState) {
      return;
    }

    for (let block: Blockly.serialization.blocks.State | null = blockState; block; block = block.next?.block ?? null) {
      block.collapsed = true;
      for (let input of Object.values(block.inputs ?? {})) {
        if (input.block && input.block.inputs) {
          input.block.collapsed = true;
        }
      }
    }
    delete blockState.collapsed;

    const isPrivate = !cardScript.public;
    if (!isPrivate) {
      delete cardScript.public;
    }

    await saveCard({
      variables: {
        cardId,
        cardScript,
        blocklyWorkspace: blockState,
      },
    });
  };

  const onDelete = async (block: BlockSvg) => {
    const cardId = block.getFieldValue("id");
    block.dispose(true, true);

    const mainWorkspace1 = mainWorkspace();
    if (!mainWorkspace1) {
      return;
    }
    for (let blockSvg of mainWorkspace1.getTopBlocks(true).filter((block) => block.getFieldValue("id") === cardId)) {
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

  const generateCard = (p: string) => {
    if (!p) return;

    let cardId = null;
    let card = null;

    if (p.startsWith("{")) {
      card = JSON.parse(p);
    } else if (p.startsWith("www")) {
      cardId = p.split("cards/")[1];
    } else if (p.includes("_") || p.includes("-")) {
      cardId = p;
    }

    if (cardId && !card) {
      if (cardId in data.classes) {
        card = data.classes[cardId];
      } else {
        generateCardAsync(cardId);
      }
    }
    const workspace = mainWorkspace();
    if (!!card && !!workspace) {
      JsonConversionUtils.generateCard(workspace, card);
    }
  };

  const generateCardAsync = (cardId: string) =>
    getCard({ variables: { id: cardId } }).then((value) => {
      const card = value.data?.getLatestCard;
      const workspace = mainWorkspace();
      if (!!card && !!workspace) {
        if (card.blocklyWorkspace) {
          const dom = Blockly.utils.xml.textToDom(card.blocklyWorkspace);
          return Blockly.Xml.domToBlock(dom, workspace);
        } else if (card.cardScript) {
          return JsonConversionUtils.generateCard(workspace, card.cardScript);
        }
      }

      return null;
    });

  const [deleteArt] = useDeleteArtMutation();

  const deleteArtAsync = async (hash: string) => {
    if (!userId) return;

    await deleteArt({ variables: { hash, owner: userId } });
    await data.refreshGeneratedArt!();
  };

  useEffect(() => {
    getToolbox()?.render(toolbox);
    mainWorkspace()?.refreshToolboxSelection();
    onWorkspaceChanged();
  }, [data.myCards]);

  useEffect(() => {
    const category = getToolbox()
      ?.getToolboxItems()
      .find((value: IToolboxItem) => {
        if (value && "getName" in value && typeof value.getName === "function") {
          const value1 = value as ISelectableToolboxItem;
          return value1.getName() === "Create your Own";
        }
        return false;
      }) as ToolboxCategory;
    category?.updateFlyoutContents(generatedArtCategory(data));

    mainWorkspace()?.refreshToolboxSelection();
    onWorkspaceChanged();
  }, [data.generatedArt]);

  // Run once before the workspace has been created
  useComponentDidMount(() => {
    const blocklyInited: {
      spellsourceInit?: boolean;
    } & object = Blockly;
    if (!blocklyInited["spellsourceInit"]) {
      BlocklyRegister.registerAll({ onSave, onDelete, onPublish, generateCardAsync, deleteArtAsync });
      BlocklyModification.modifyAll();
      BlocklyMiscUtils.initBlocks(data);
      BlocklyMiscUtils.initHeroClassColors(data);
      BlocklyMiscUtils.initArtBlcks(data);
      SpellsourceGenerator.generateJavaScript();
      blocklyInited["spellsourceInit"] = true;
    }
  });

  // Run once after the workspace has been created
  useEffectOnce(() => {
    mainWorkspace()?.getTheme().setStartHats(true);

    BlocklyToolbox.initCallbacks(mainWorkspace()!);

    let url = new URL(window.location.href);
    let params = url.searchParams;
    let card = params.get("card");
    if (card) {
      generateCard(card);
      params.delete("card");
      history.replaceState(null, "", url.toString());
    }

    // Let you ctrl S to save
    const listener = (evt: KeyboardEvent) => {
      if (evt.key === "s" && (evt.metaKey || evt.ctrlKey)) {
        evt.preventDefault();
        cancelDebounce(); // Immediately run saveAll, skipping next one
        saveAll();
      }

      if (evt.key === "o" && (evt.metaKey || evt.ctrlKey)) {
        evt.preventDefault();
        openFromFile.callback({ workspace: mainWorkspace()!, block: undefined });
      }
    };
    document.addEventListener("keydown", listener);
    return () => document.removeEventListener("keydown", listener);
  });

  //Switch the renderer
  useEffect(() => {
    if (props.renderer) {
      BlocklyMiscUtils.switchRenderer(props.renderer, mainWorkspace()!);
    }
  }, [props.renderer]);

  //The default workspace changed event handler
  const onWorkspaceChanged = () => {
    if (!mainWorkspace()) {
      return;
    }
    if (mainWorkspace()!.isDragging()) return; // While dragging a block, it doesn't appear in the blocks list

    const cardScript = handleWorkspaceCards(mainWorkspace()!, data.userId || "guest", getCard);
    BlocklyMiscUtils.pluralStuff(mainWorkspace()!);
    let json = JSON.stringify(cardScript, null, 2);
    setJson(json);
  };

  const toolbox = useMemo(() => BlocklyToolbox.editorToolbox(results, data), [results, data]);

  const xs = !useBreakpoint("sm", "up");

  // Upload art via drag & drop
  useDrop({
    onFiles: async (files, event) => {
      if (!userId) return;

      for (const file of files) {
        if (!file.type.includes("png") && !file.type.includes("jpeg")) {
          continue;
        }

        const response = await fetch(window.location.origin + "/api/art/upload", {
          method: "POST",
          body: file,
          headers: {
            "Content-Type": file.type,
          },
        });

        const uuid = (await response.json()) as string;

        const url = `/api/art/uploaded/${userId}/${uuid}`;

        const artGenerated = newBlock(mainWorkspace()!, "Art_Generated");
        (artGenerated as BlockSvg).initSvg();
        artGenerated.setFieldValue(url, "src");
        artGenerated.setFieldValue(uuid, "hash");

        // TODO merge with serverside postgraphile querying
        await data.saveGeneratedArt!({
          variables: {
            hash: uuid,
            urls: [url],
            info: { name: file.name, size: file.size, lastModified: file.lastModified },
          },
        });
      }

      mainWorkspace()!.render();
      await data.refreshGeneratedArt!();
    },
  });

  return (
    <SimpleReactBlockly
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={cx("h-100")}
      workspaceConfiguration={{
        disable: false,
        zoom: {
          controls: true,
          minScale: 0.5,
          maxScale: 2.0,
          pinch: true,
          wheel: true,
          startScale: xs ? 0.75 : 1,
        },
        move: {
          wheel: true,
        },
        oneBasedIndex: false,
        horizontalLayout: xs,
        renderer: props.renderer || SpellsourceRenderer.name,
        toolbox,
        plugins,
      }}
      ref={blocklyEditor}
      init={(workspace) => initWorkspace(workspace, data)}
    />
  );
});

export default CardEditorWorkspace;
