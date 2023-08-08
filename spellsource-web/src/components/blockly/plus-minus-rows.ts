import { ExtensionMixin, MutatorFn } from "./mutators";
import Blockly, { Block } from "blockly";
import { BlockArgDef, BlockDef } from "../../__generated__/blocks";
import { reInitBlock } from "../../lib/blockly-misc-utils";
import { rowsList } from "../../lib/json-conversion-utils";

interface PlusMinusOptions {
  arrayName: string;
  itemCount?: number;
}

interface PlusMinusState {
  itemCount: number;
}

export interface PlusMinusRowsMutator extends ExtensionMixin<PlusMinusRowsMutator, PlusMinusState>, PlusMinusState {
  options: PlusMinusOptions;

  updateShape_(this: PlusMinusRowsMutator & Block): void;

  plus(this: PlusMinusRowsMutator & Block, index: number): void;

  minus(this: PlusMinusRowsMutator & Block, index: number): void;

  initMutator(this: PlusMinusRowsMutator & Block): void;
}

export const PlusMinusRows = "plus_minus_rows";

export const PlusMinusRowsMixin: PlusMinusRowsMutator = {
  itemCount: null,
  options: null,
  mutationToDom(): Element {
    const container = Blockly.utils.xml.createElement("mutation");
    container.setAttribute("itemCount", String(this.itemCount));
    container.setAttribute("arrayName", this.options.arrayName);
    return container;
  },
  domToMutation(xmlElement: Element): void {
    this.itemCount = parseInt(xmlElement.getAttribute("itemCount"), 10);
    this.updateShape_();
  },
  saveExtraState(): PlusMinusState {
    return { itemCount: this.itemCount };
  },
  loadExtraState(state: PlusMinusState) {
    this.itemCount = state.itemCount;
    this.updateShape_();
  },
  updateShape_(): void {
    const block = JSON.parse(JSON.stringify(Blockly.Blocks[this.type].json)) as BlockDef;
    delete block.mutator;

    const emptyMessage = block.message0;
    const emptyArgs = block.args0;
    const firstMessage = block.message1;
    const firstArgs = block.args1;
    const restMessage = block.message2;
    const restArgs = block.args2;

    rowsList(block).forEach((row, i) => {
      delete block[`message${i}`];
      delete block[`args${i}`];
    });

    const rows = [] as [string, BlockArgDef[]][];

    const argNumbers = (args: BlockArgDef[], i: number) => {
      const newArgs = JSON.parse(JSON.stringify(args)) as BlockArgDef[];

      newArgs.find((arg) => arg.type === "input_value").name = this.options.arrayName + i;

      return newArgs;
    };

    if (this.itemCount == 0) {
      rows.push([emptyMessage, emptyArgs]);
    } else {
      rows.push([firstMessage, argNumbers(firstArgs, 0)]);

      for (let i = 1; i < this.itemCount; i++) {
        rows.push([restMessage, argNumbers(restArgs, i)]);
      }
    }

    rows.forEach(([message, args], i) => {
      block[`message${i}`] = message;
      block[`args${i}`] = args;
    });

    reInitBlock(this, block);
  },
  minus(index: number): void {
    const block = Blockly.Blocks[this.type].json as BlockDef;
    const options = block.mutatorOptions as PlusMinusOptions;

    let count = 0;
    this.inputList.forEach((input, i) => {
      if (i == index) {
        input.name = "DELETING";
      } else {
        input.name = this.options.arrayName + count++;
      }
    });

    this.itemCount--;
    if (this.itemCount < 0) {
      this.itemCount = 0;
    } else {
      this.updateShape_();
    }
  },
  plus(index: number): void {
    const block = Blockly.Blocks[this.type].json as BlockDef;
    const options = block.mutatorOptions as PlusMinusOptions;

    let count = 0;
    this.inputList.forEach((input, i) => {
      input.name = this.options.arrayName + count;
      if (index == i) {
        count += 2;
      } else {
        count++;
      }
    });

    this.itemCount++;
    this.updateShape_();
  },
  initMutator(this): void {
    const block = Blockly.Blocks[this.type].json as BlockDef;
    this.options = block.mutatorOptions as PlusMinusOptions;

    if (this.itemCount == null) {
      this.itemCount = this.options.itemCount ?? 0;
    }
  },
};

export const PlusMinusRowsFn: MutatorFn<PlusMinusRowsMutator> = function (this) {
  this.initMutator();
  this.updateShape_();
};
