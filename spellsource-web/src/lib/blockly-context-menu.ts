import * as Blockly from "blockly";
import {Block, BlockSvg, ContextMenuRegistry, ISelectableToolboxItem, Toolbox, Workspace} from "blockly";
import * as JsonConversionUtils from "./json-conversion-utils";
import * as WorkspaceUtils from "./workspace-utils";
import * as BlocklyMiscUtils from "./blockly-spellsource-utils";
import { newBlock, searchToolbox } from "./blockly-utils";
import { InitBlockOptions } from "../components/card-editor-workspace";
import { openFile, saveFile } from "./file-dialogs";
import {CardDef} from "../components/collection/card-display";

export const registerAll = (options: InitBlockOptions) => {
  ContextMenuRegistry.registry.register(importCardScript(options));
  ContextMenuRegistry.registry.register(showInToolbox);
  ContextMenuRegistry.registry.register(getReferenceBlock);
  ContextMenuRegistry.registry.register(logCardscript);
  ContextMenuRegistry.registry.register(saveToFile);
  ContextMenuRegistry.registry.register(deleteCard(options));
  ContextMenuRegistry.registry.register(publishCard(options));

  ContextMenuRegistry.registry.register(openFromFile);

  ContextMenuRegistry.registry.unregister("blockInline");
};

const importCardScript = ({ generateCardAsync }: InitBlockOptions): ContextMenuRegistry.RegistryItem => ({
  id: "importCardscript",
  displayText: "Import Cardscript",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) =>
    block?.type.startsWith("CatalogueCard") || block?.type === "Card_REFERENCE" ? "enabled" : "hidden",
  callback: (clb) => {
    const block = clb.block!;
    let workspace = block.workspace;
    if (workspace.targetWorkspace) {
      workspace = workspace.targetWorkspace;
    }

    if ("json" in block && "json" in (block["json"] as object)) {
      let card = (block.json as any)["json"] as CardDef;
      let dummyWorkspace = new Workspace();
      JsonConversionUtils.generateCard(dummyWorkspace, card);

      let top = dummyWorkspace.getTopBlocks(false)[0];

      let xml = Blockly.Xml.blockToDom(top, true) as Element;
      var xy = top.getRelativeToSurfaceXY();
      xml.setAttribute("x", String(xy.x));
      xml.setAttribute("y", String(xy.y));

      workspace.paste(xml);
      dummyWorkspace.dispose();
    } else if (block.type === "Card_REFERENCE") {
      const cardId = block.getFieldValue("id");

      generateCardAsync(cardId);
    }

    const toolbox = workspace.getToolbox() as Toolbox;
    toolbox.clearSelection();
  },
});

const saveToFile: ContextMenuRegistry.RegistryItem = {
  id: "saveToFile",
  displayText: "Save to File",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: () => (process.env.NODE_ENV === "production" ? "hidden" : "enabled"),
  callback: ({ block }) => {
    const blockJson = Blockly.serialization.blocks.save(block!);

    saveFile(
      JSON.stringify(blockJson, null, 2),
      "application/json",
      (block?.getInputTargetBlock("name")?.getFieldValue("text") || block?.type) + ".json"
    );
  },
};

export const openFromFile: ContextMenuRegistry.RegistryItem = {
  id: "openFromFile",
  displayText: "Open from File",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.WORKSPACE,
  preconditionFn: ({ workspace }) => (workspace!.targetWorkspace ? "hidden" : "enabled"),
  callback: ({ workspace }) =>
    openFile(".json", (result) => {
      const blockJson = JSON.parse(result);

      const block = Blockly.serialization.blocks.append(blockJson, workspace!);

      block.setCollapsed(false);

      block.bumpNeighbours();
    }),
};

const showInToolbox: ContextMenuRegistry.RegistryItem = {
  id: "showInToolbox",
  displayText: "Show In Toolbox",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) =>
    !block?.workspace.targetWorkspace ||
    (block.workspace.targetWorkspace?.getToolbox()?.getSelectedItem() as ISelectableToolboxItem).getName() === "Search"
      ? "enabled"
      : "hidden",
  callback: ({ block }) => {
    searchToolbox(block?.type, block?.workspace.targetWorkspace || block?.workspace!);
  },
};

const getReferenceBlock: ContextMenuRegistry.RegistryItem = {
  id: "getReferenceBlock",
  displayText: "Get Reference Block",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block?.type.startsWith("Starter_") ? "enabled" : "hidden"),
  callback: (clb) => {
    const block = clb.block! as Block
    const workspace = block.workspace.isFlyout ? block.workspace.targetWorkspace : block.workspace;
    const card = block.cardScript || WorkspaceUtils.blockToCardScript(block);

    const refBlock: BlockSvg = newBlock(
      workspace,
      block.type == "Starter_CLASS" ? "HeroClass_REFERENCE" : "Card_REFERENCE"
    ) as BlockSvg;
    refBlock.setFieldValue(block.getFieldValue("id"), "id");
    refBlock.setFieldValue(BlocklyMiscUtils.cardMessage(card), "name");

    refBlock.initSvg();
    BlocklyMiscUtils.refreshBlock(refBlock);

    refBlock.moveTo(block.getRelativeToSurfaceXY());
    refBlock.select();
  },
};

const logCardscript: ContextMenuRegistry.RegistryItem = {
  id: "logCardscript",
  displayText: "Log Cardscript",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) =>
    block?.type.startsWith("Starter_") && process.env.NODE_ENV !== "production" ? "enabled" : "hidden",
  callback: ({ block }) => {
    console.log(WorkspaceUtils.blockToCardScript(block!));
  },
};

const deleteCard = ({ onDelete }: InitBlockOptions): ContextMenuRegistry.RegistryItem => ({
  id: "deleteCard",
  displayText: "Permanently Delete Card",
  weight: 7,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block?.type.startsWith("Starter_") ? "enabled" : "hidden"),
  callback: ({ block }) => {
    if (confirm("Remove this card from database?")) {
      onDelete(block!);
    }
  },
});

const publishCard = ({ onPublish }: InitBlockOptions): ContextMenuRegistry.RegistryItem => ({
  id: "publishCard",
  displayText: "Publish Card",
  weight: 8,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block?.type.startsWith("Starter_") ? "enabled" : "hidden"),
  callback: ({ block }) => {
    if (confirm("Publish this card / its changes for other players to use?")) {
      onPublish(block!);
    }
  },
});
