import Blockly, { Block, BlockSvg, icons } from "blockly";
import { MutatorFn, MutatorMixin } from "./mutators";
import { newBlock, reInitBlock } from "../../lib/blockly-misc-utils";
import { BlockArgDef, BlockDef } from "../../__generated__/blocks";
import { rowsList } from "../../lib/json-conversion-utils";

export interface OptionalRowsOptions {
  optional: Record<`${number}`, string>;
  defaults?: Record<string, boolean>;
}

interface OptionalRowsMutator extends MutatorMixin<OptionalRowsMutator, Record<string, boolean>> {
  args: Record<string, boolean>;

  initMutator(this: OptionalRowsMutator & Block): void;
}

export const OptionalRows = "optional_rows";

export const OptionalRowsMixin: OptionalRowsMutator = {
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
  saveExtraState() {
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

    // Rebuild block with only toggled args
    const options = block.mutatorOptions as OptionalRowsOptions;
    const rows = [] as [string, BlockArgDef[]][];
    rowsList(block).forEach((row, i) => {
      const key = options.optional[i.toString()];
      if (!key || typeof key !== "string" || this.args[key]) {
        rows.push(row);
      }
      delete block[`message${i}`];
      delete block[`args${i}`];
    });
    rows.forEach(([message, args], i) => {
      block[`message${i}`] = message;
      block[`args${i}`] = args;
    });

    reInitBlock(this, block);
  },
  initMutator() {
    if (this.args) return;

    this.args = {};

    const block = Blockly.Blocks[this.type].json as BlockDef;

    const options = block.mutatorOptions as OptionalRowsOptions;

    for (let value of Object.values(options?.optional ?? {})) {
      this.args[value] = options.defaults?.[value] ?? true;
    }
  },
};

export const OptionalRowsFn: MutatorFn<OptionalRowsMutator> = function (this) {
  this.initMutator();
  this.setMutator(new icons.MutatorIcon([], this as BlockSvg));
  this.rebuildShape_();
};
