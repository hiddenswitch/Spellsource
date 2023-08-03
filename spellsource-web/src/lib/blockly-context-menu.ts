import * as Blockly from "blockly";
import { BlockDragger, BlockSvg, ContextMenuRegistry, Toolbox, ToolboxItem, Workspace } from "blockly";
import * as JsonConversionUtils from "./json-conversion-utils";
import { openFile, saveFile } from "./fs-utils";
import * as WorkspaceUtils from "./workspace-utils";
import * as BlocklyMiscUtils from "./blockly-misc-utils";
import { InitBlockOptions } from "../components/card-editor-workspace";

export const registerAll = (options: InitBlockOptions) => {
  ContextMenuRegistry.registry.register(importCardScript);
  ContextMenuRegistry.registry.register(showInToolbox);
  ContextMenuRegistry.registry.register(getReferenceBlock);
  ContextMenuRegistry.registry.register(logCardscript);
  ContextMenuRegistry.registry.register(saveToFile);
  ContextMenuRegistry.registry.register(deleteCard(options));
  ContextMenuRegistry.registry.register(publishCard(options));

  ContextMenuRegistry.registry.register(openFromFile);

  ContextMenuRegistry.registry.unregister("blockInline");
};

const importCardScript: ContextMenuRegistry.RegistryItem = {
  id: "importCardscript",
  displayText: "Import Cardscript",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block.type.startsWith("CatalogueCard") ? "enabled" : "hidden"),
  callback: ({ block }) => {
    let workspace = block.workspace;
    if (block.json && block.json.json) {
      let card = block.json.json;
      let dummyWorkspace = new Workspace();
      JsonConversionUtils.generateCard(dummyWorkspace, card);

      let top = dummyWorkspace.getTopBlocks(false)[0];

      let xml = Blockly.Xml.blockToDom(top, true) as Element;
      var xy = top.getRelativeToSurfaceXY();
      xml.setAttribute("x", String(xy.x));
      xml.setAttribute("y", String(xy.y));

      if (workspace.targetWorkspace) {
        workspace = workspace.targetWorkspace;
      }

      workspace.paste(xml);
      dummyWorkspace.dispose();
      (workspace.getToolbox() as Toolbox).clearSelection();
    }
  },
};

const saveToFile: ContextMenuRegistry.RegistryItem = {
  id: "saveToFile",
  displayText: "Save to File",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: () => (process.env.NODE_ENV === "production" ? "hidden" : "enabled"),
  callback: ({ block }) => {
    const xml = Blockly.Xml.blockToDom(block, false);

    // Serialize XML or DocumentFragment to string
    const serializer = new XMLSerializer();
    const xmlString = serializer.serializeToString(xml);

    saveFile(
      xmlString,
      "application/xml",
      (block.getInputTargetBlock("name")?.getFieldValue("text") || block.type) + ".xml"
    );
  },
};

export const openFromFile: ContextMenuRegistry.RegistryItem = {
  id: "openFromFile",
  displayText: "Open from File",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.WORKSPACE,
  preconditionFn: ({ workspace }) => (workspace.targetWorkspace ? "hidden" : "enabled"),
  callback: ({ workspace }) =>
    openFile(".xml", (result) => {
      const dom = Blockly.utils.xml.textToDom(result);

      for (const nextBlock of dom.getElementsByTagName("block")) {
        for (const childNode of nextBlock.childNodes) {
          const tagName = (childNode as Element).tagName;
          if (tagName !== "data" && tagName !== "field" && tagName !== "next") {
            nextBlock.setAttribute("collapsed", "false");
            break;
          }
        }
      }

      const block = Blockly.Xml.domToBlock(dom, workspace);
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
    !block.workspace.targetWorkspace ||
    block.workspace.targetWorkspace?.getToolbox()?.getSelectedItem()?.["name_"] === "Search"
      ? "enabled"
      : "hidden",
  callback: ({ block }) => {
    BlocklyMiscUtils.searchToolbox(block.type, block.workspace.targetWorkspace || block.workspace);
  },
};

const getReferenceBlock: ContextMenuRegistry.RegistryItem = {
  id: "getReferenceBlock",
  displayText: "Get Reference Block",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block.type.startsWith("Starter_") ? "enabled" : "hidden"),
  callback: ({ block }) => {
    const workspace = block.workspace.isFlyout ? block.workspace.targetWorkspace : block.workspace;
    const card = block.cardScript || WorkspaceUtils.xmlToCardScript(Blockly.Xml.blockToDom(block, true));

    const newBlock: BlockSvg = BlocklyMiscUtils.newBlock(
      workspace,
      block.type == "Starter_CLASS" ? "HeroClass_REFERENCE" : "Card_REFERENCE"
    ) as BlockSvg;
    newBlock.setFieldValue(block.getFieldValue("id"), "id");
    newBlock.setFieldValue(BlocklyMiscUtils.cardMessage(card), "name");

    newBlock.initSvg();
    BlocklyMiscUtils.refreshBlock(newBlock);

    newBlock.moveTo(block.getRelativeToSurfaceXY());
    newBlock.select();
  },
};

const logCardscript: ContextMenuRegistry.RegistryItem = {
  id: "logCardscript",
  displayText: "Log Cardscript",
  weight: 6,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) =>
    block.type.startsWith("Starter_") && process.env.NODE_ENV !== "production" ? "enabled" : "hidden",
  callback: ({ block }) => {
    const xml = Blockly.Xml.blockToDom(block, true);
    const json = WorkspaceUtils.xmlToCardScript(xml);
    console.log(json);
  },
};

const deleteCard = ({ onDelete }: InitBlockOptions): ContextMenuRegistry.RegistryItem => ({
  id: "deleteCard",
  displayText: "Permanently Delete Card",
  weight: 7,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block.type.startsWith("Starter_") ? "enabled" : "hidden"),
  callback: ({ block }) => {
    if (confirm("Remove this card from database?")) {
      onDelete(block);
    }
  },
});

const publishCard = ({ onPublish }: InitBlockOptions): ContextMenuRegistry.RegistryItem => ({
  id: "publishCard",
  displayText: "Publish Card",
  weight: 8,
  scopeType: ContextMenuRegistry.ScopeType.BLOCK,
  preconditionFn: ({ block }) => (block.type.startsWith("Starter_") ? "enabled" : "hidden"),
  callback: ({ block }) => {
    if (confirm("Publish this card / its changes for other players to use?")) {
      onPublish(block);
    }
  },
});
