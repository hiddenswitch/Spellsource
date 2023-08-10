import { BlockArgDef, BlockDef, BlocklyShadowState } from "./blockly-types";
import Blockly, { Block, BlockSvg, Connection, Toolbox, ToolboxCategory, WorkspaceSvg } from "blockly";
import * as JsonConversionUtils from "./json-conversion-utils";
import { OptionalRows, OptionalRowsOptions } from "../components/blockly/optional-rows";
import { BlockInfo, FlyoutItemInfo } from "blockly/core/utils/toolbox";
import { ConnectionState } from "blockly/core/serialization/blocks";

/**
 * Returns a list of the all the arguments in a block's json
 * @param block
 * @param type
 * @returns An array of the args
 */
export const argsList = (block: BlockDef, type?: "input" | "dropdown" | "number" | string) => {
  let argsList = [] as BlockArgDef[];

  for (let i = 0, args: BlockArgDef[]; (args = block[`args${i}`]); i++) {
    argsList.push(...args.filter((arg) => !type || arg.type?.includes(type)));
  }

  return argsList;
};

/**
 * Gets all the rows (message/arg pairs) on a block
 * @param block
 */
export const rowsList = (block: BlockDef) => {
  const list = [] as [string, BlockArgDef[]][];

  for (
    let i = 0, message: string, args: BlockArgDef[];
    (message = block[`message${i}`]) && (args = block[`args${i}`]);
    i++
  ) {
    list.push([message, args]);
  }

  return list;
};

/**
 * initializes the json specified shadow blocks of a block on the workspace
 * @param thisBlock
 * @param block
 * @param allowNonShadow
 */
export const manuallyAddShadowBlocks = (thisBlock: Block, block: BlockDef, allowNonShadow = true) => {
  for (let arg of argsList(block)) {
    addShadowBlocksToConnection(thisBlock.getInput(arg.name)?.connection, arg, allowNonShadow);
  }

  addShadowBlocksToConnection(thisBlock.nextConnection, block.next, allowNonShadow);
};

export const addShadowBlocksToConnection = (
  connection: Connection | null,
  arg?: BlocklyShadowState,
  allowNonShadow = true
) => {
  const shadow = arg?.shadow ?? arg?.block;
  if (!shadow || !connection) return;

  const prevBlock = connection.targetBlock();
  connection.setShadowState(shadow);
  const currentBlock = connection.targetBlock();

  if (prevBlock !== currentBlock) {
    if (allowNonShadow && arg.block && currentBlock.isShadow()) {
      currentBlock.setShadow(false);
      connection.setShadowState(shadow);
    }
    manuallyAddShadowBlocks(currentBlock, Blockly.Blocks[shadow.type].json, allowNonShadow);
  }
};

/**
 * Updates a block in the workspace based on new json definition
 * @param block
 * @param state
 */
export const reInitBlock = (block: Block, state: BlockDef) => {
  // Save connections / shadow blocks
  const connections = {} as Record<string, Connection | undefined>;
  for (const input of block.inputList.slice()) {
    const connection = (connections[input.name] = input.connection?.targetConnection);
    block.removeInput(input.name, true);
  }

  // Init again
  block.jsonInit(state);

  // Restore connections / shadow blocks
  for (let [name, connection] of Object.entries(connections)) {
    if (block.getInput(name)) {
      connection?.reconnect(block, name);
    }
  }
  manuallyAddShadowBlocks(block, state, false);

  block.bumpNeighbours();
};

/**
 * Helper method to make sure added blocks have the correct shadows
 * We still want those in case people decide to pull apart the converted stuff
 * @param workspace The workspace
 * @param type The block type to create
 * @returns The created block
 */
export function newBlock(workspace, type): Block | BlockSvg {
  let block = workspace.newBlock(type);
  manuallyAddShadowBlocks(block, Blockly.Blocks[type].json);
  return block;
}

/**
 * Adds a block to Blockly
 * @param block
 */
export const addBlock = (block: BlockDef) => {
  JsonConversionUtils.addBlockToMap(block);

  if (block.mutator === OptionalRows) {
    const newBlock: BlockDef = {
      type: block.type + "_container",
      message0: block.message0,
      args0: block.args0,
      colour: block.colour,
      nextStatement: null,
    };

    const options = block.mutatorOptions as OptionalRowsOptions;

    const args = {} as Record<string, boolean>;

    for (let value of Object.values(options.optional)) {
      args[value] = options.defaults?.[value] ?? true;
    }

    Object.entries(args).forEach(([name, checked], index) => {
      newBlock[`message${index}`] = `${name}: %1`;
      newBlock[`args${index}`] = [
        {
          type: "field_checkbox",
          name,
          checked,
        },
      ];
    });

    addBlock(newBlock);
  }

  return (Blockly.Blocks[block.type!] = {
    init: function (this: Block) {
      this.jsonInit(block);
      if (block.data) {
        this.data = block.data;
      }
      if (block.hat) {
        this.hat = block.hat;
      }
      if (block.type!.endsWith("SHADOW")) {
        this.setMovable(false);
      }
      this.setInputsInline(false);
    },
    json: block,
    data: block.data,
  });
};

/**
 * Shows where a block in the workspace is within the toolbox
 * @param blockType
 * @param mainWorkspace
 */
export const searchToolbox = (blockType, mainWorkspace: WorkspaceSvg) => {
  let toolbox = mainWorkspace.getToolbox() as Toolbox;
  let categories = toolbox.getToolboxItems().slice(1) as ToolboxCategory[];

  for (let category of categories) {
    if (category.getContents) {
      let contents = category.getContents();
      for (let content of contents as FlyoutItemInfo[]) {
        if (content.kind === "block" && (content as BlockInfo).type === blockType) {
          if (category.getParent() && category.getParent().isCollapsible() && !category.getParent().isExpanded()) {
            category.getParent().toggleExpanded();
          }
          toolbox.setSelectedItem(category);

          const flyOut = toolbox.getFlyout();

          let workspace: WorkspaceSvg;

          if ((workspace = flyOut.getWorkspace())) {
            let totalHeight = 0;
            for (let topBlock of workspace.getTopBlocks(true)) {
              if (topBlock.type === blockType) {
                workspace.scrollbar.setY(totalHeight);
                topBlock.addSelect();
              } else {
                totalHeight += topBlock.height + 24;
              }
            }
          }

          return;
        }
      }
    }
  }
};

export const getBlockInputs = (type: string, allowNonShadow = true) => {
  const block = Blockly.Blocks[type];
  const inputs = {} as Record<string, any>;

  if (!block || !block.json) return inputs;
  const json = block.json as BlockDef;

  rowsList(json).forEach(([, args]) => {
    for (let arg of args) {
      const name = arg?.name;
      if (!name) continue;

      if (arg.block && allowNonShadow) {
        const input = (inputs[name] ??= {} as any);
        input.block = { ...arg.block };
        input.block.inputs = getBlockInfo(input.block.type).inputs;

        if (!arg.shadow && !Object.values(input.block.inputs).some((input: ConnectionState) => input.block)) {
          arg.shadow = arg.block;
        }
      }

      if (arg.shadow) {
        const input = (inputs[name] ??= {} as any);
        input.shadow = { ...arg.shadow };
        input.shadow.inputs = getBlockInfo(input.shadow.type).inputs;
      }
    }
  });

  return inputs;
};

export const getBlockInfo = (type: string): BlockInfo =>
  (Blockly.Blocks[type].toolboxInfo ??= {
    type,
    kind: "block",
    inputs: getBlockInputs(type),
    next: Blockly.Blocks[type]?.json?.next,
  });
