import Blockly, { Block, BlockSvg, Connection, icons } from "blockly";
import { MutatorFn, MutatorMixin } from "./mutators";
import { manuallyAddShadowBlocks, newBlock } from "../../lib/blockly-misc-utils";
import { BlockArgDef, BlockDef } from "../../__generated__/blocks";
import { argsList, messagesAndArgs } from "../../lib/json-conversion-utils";

interface TestMutator extends MutatorMixin<TestMutator, Record<string, boolean>> {
  args: Record<string, boolean>;

  initTestMutator(this: TestMutator & Block): void;
}

export const TestMutatorName = "test_mutator";

export const TestMutatorMixin: TestMutator = {
  args: undefined,
  mutationToDom() {
    const container = Blockly.utils.xml.createElement("mutation");
    for (let [arg, value] of Object.entries(this.args)) {
      container.setAttribute(arg, String(value).toUpperCase());
    }
    return container;
  },
  domToMutation(this, xmlElement: Element): void {
    this.args = {};

    for (let arg of xmlElement.getAttributeNames()) {
      this.args[arg] = xmlElement.getAttribute(arg) == "TRUE";
    }

    this.rebuildShape_();
  },
  saveExtraState(): {} {
    return this.args;
  },
  loadExtraState(state) {
    this.args = state;
    this.rebuildShape_();
  },
  decompose(workspace): Block {
    const block = newBlock(workspace, this.type + "_container");
    (block as BlockSvg).initSvg();

    for (let [arg, enabled] of Object.entries(this.args)) {
      block.setFieldValue(enabled, arg);
    }

    return block;
  },
  compose(block): void {
    for (let arg of Object.keys(this.args)) {
      this.args[arg] = block.getFieldValue(arg) == "TRUE";
    }

    this.rebuildShape_();
  },
  rebuildShape_() {
    const block = JSON.parse(JSON.stringify(Blockly.Blocks[this.type].json)) as BlockDef;
    delete block.mutator;

    // Save connections / shadow blocks
    const connections = {} as Record<string, Connection | undefined>;
    const shadows = {} as Record<string, boolean | undefined>;
    for (const input of this.inputList.slice()) {
      const connection = (connections[input.name] = input.connection?.targetConnection);
      if (connection) {
        shadows[input.name] = connection.getSourceBlock()?.isShadow();
        connection.getSourceBlock()?.setShadow(false);
      }
      this.removeInput(input.name);
    }

    // Rebuild block with only toggled args
    const allMessagesAndArgs = [] as [string, BlockArgDef[]][];
    messagesAndArgs(block).forEach(([message, args], i) => {
      if (!args.some((arg) => arg.optional && !this.args[arg.name])) {
        allMessagesAndArgs.push([message, args]);
      }
      delete block[`message${i}`];
      delete block[`args${i}`];
    });
    allMessagesAndArgs.forEach(([message, args], i) => {
      block[`message${i}`] = message;
      block[`args${i}`] = args;
    });
    this.jsonInit(block);

    // Restore connections / shadow blocks
    for (let [name, connection] of Object.entries(connections)) {
      if (this.getInput(name)) {
        connection?.reconnect(this, name);
        connection?.getSourceBlock()?.setShadow(shadows[name] || false);
      } else if (shadows[name]) {
        connection?.getSourceBlock()?.dispose(true);
      }
    }
    manuallyAddShadowBlocks(this, Blockly.Blocks[this.type].json);

    this.bumpNeighbours();
  },
  initTestMutator() {
    if (this.args) return;

    this.args = {};

    const block = Blockly.Blocks[this.type].json as BlockDef;

    for (let arg of argsList(block).filter((arg) => arg.optional)) {
      this.args[arg.name] = true;
    }
  },
};

export const TestMutatorFn: MutatorFn<TestMutator> = function (this) {
  this.initTestMutator();
  this.setMutator(new icons.MutatorIcon([], this as BlockSvg));
};
