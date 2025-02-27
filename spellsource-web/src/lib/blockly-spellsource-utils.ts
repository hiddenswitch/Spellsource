import Blockly, { Block, BlockSvg, Field, WorkspaceSvg } from "blockly";
import * as JsonConversionUtils from "./json-conversion-utils";
import { CardDef } from "../components/collection/card-display";
import { BlocklyDataContext } from "../pages/card-editor";
import { ContextType } from "react";
import { BlockDef, heroClassColors, textColor } from "./blockly-types";
import { addBlock, argsList, argsList as argsList1 } from "./blockly-utils";
import { FieldInput } from "blockly/core/field_input";

export const toTitleCaseCorrected = (string: string) =>
  string
    .split(/[_ ]/)
    .map((w) => w[0].toUpperCase() + w.substring(1).toLowerCase())
    .join(" ")
    .replace("Hero Power", "Skill")
    .replace("Minion", "Unit")
    .replace("Weapon", "Item")
    .replace("Hero", "Champion");

//make the message for a generated block for a catalogue/created card
export function cardMessage(card: CardDef) {
  let ret = "";
  if (card.baseManaCost || card.baseManaCost === 0) {
    ret = "(" + card.baseManaCost + ") ";
  }
  if (card.type === "MINION") {
    ret += (card.baseAttack ?? 0) + "/" + (card.baseHp ?? 0);
  } else if (card.type === "CLASS") {
    return card.name;
  } else {
    ret += toTitleCaseCorrected(card.type);
  }
  ret += ' "' + card.name + '"';
  return ret;
}

export function initBlocks(data: ContextType<typeof BlocklyDataContext>) {
  const icons = Object.fromEntries(data.allIcons.map((icon) => [icon.name, icon.src]));

  // All of our spells, triggers, entity reference enum values, etc.
  data.allBlocks?.forEach((block) => {
    if (block.type! in Blockly.Blocks) {
      return;
    }

    if (block.output && !JsonConversionUtils.blockTypeColors[block.output as string] && block.colour) {
      JsonConversionUtils.blockTypeColors[block.output as string] = block.colour;
    }

    for (let arg of argsList(block, "field_image")) {
      if (arg.src && arg.src in icons) {
        arg.src = icons[arg.src];
      }
    }

    addBlock(block);
  });
}

export function initHeroClassColors(data: ContextType<typeof BlocklyDataContext>) {
  /**
   * first pass through the card catalogue to figure out all the collectible
   * hero classes and their colors
   */
  Object.values(data.classes).forEach((card) => {
    setupHeroClassColor(card);
  });

  Object.values(data.myCards).forEach((value) => {
    const card = value.cardScript as CardDef;
    if (card.type === "CLASS") {
      setupHeroClassColor(card);
    }
  });
}

export function setupHeroClassColor(card: CardDef) {
  if (card.art?.body?.vertex) {
    textColor[card.heroClass] = Blockly.utils.colour.rgbToHex(
      card.art.body.vertex.r * 255,
      card.art.body.vertex.g * 255,
      card.art.body.vertex.b * 255
    );
  }

  if (card.art?.primary) {
    return (heroClassColors[card.heroClass] = Blockly.utils.colour.rgbToHex(
      card.art.primary.r * 255,
      card.art.primary.g * 255,
      card.art.primary.b * 255
    ));
  }

  return "#888888";
}

export function colorToHex(colour: string) {
  var hue = Number(colour);
  if (!isNaN(hue)) {
    return Blockly.utils.colour.hueToHex(hue);
  } else {
    return Blockly.utils.colour.parse(colour);
  }
}

/*export function secondaryColor(color) {
  return Blockly.blockRendering.ConstantProvider.prototype.generateSecondaryColour_(color)
}

export function tertiaryColor(color) {
  return Blockly.blockRendering.ConstantProvider.prototype.generateTertiaryColour_(color)
}*/

export function loadableInit(blockly: typeof Blockly) {
  setTimeout(() => {
    const all = blockly.Workspace.getAll();
    for (let i = 0; i < all.length; i++) {
      const workspace = all[i];
      if (!workspace.getRootWorkspace() && workspace.rendered) {
        Blockly.svgResize(workspace as WorkspaceSvg);
      }
    }
  }, 1);
}

type WorkspaceWithPrivateRenderer = WorkspaceSvg & {
  name: string;
  renderer_: Blockly.blockRendering.Renderer;
};

export function switchRenderer(renderer: string, workspace1: WorkspaceSvg) {
  const workspace = workspace1 as WorkspaceWithPrivateRenderer;
  if (renderer !== workspace.getRenderer()["name"]) {
    workspace.renderer_ = Blockly.blockRendering.init(
      renderer,
      workspace.getTheme(),
      workspace.options.rendererOverrides ?? undefined
    );

    const toolboxFlyoutWorkspace = workspace.getToolbox()?.getFlyout()?.getWorkspace() as WorkspaceWithPrivateRenderer;
    toolboxFlyoutWorkspace.renderer_ = Blockly.blockRendering.init(
      renderer,
      toolboxFlyoutWorkspace.getTheme(),
      workspace.options.rendererOverrides ?? undefined
    );

    workspace.refreshTheme();
  }
}

type FieldInputWithPrivateEditor = FieldInput<any> & {
  // re-export protected fields
  isBeingEdited_: boolean;
  showEditor_: (_e?: Event, quietInput?: boolean) => void;
};

export function pluralStuff(workspace: WorkspaceSvg) {
  let anyChange = false;
  for (let block of workspace.getAllBlocks(true)) {
    if (!block.json) {
      continue;
    }
    let argsList = argsList1(block.json);
    for (let arg of argsList) {
      if (arg.type === "field_label_plural") {
        let shouldBePlural = null;
        let connection;
        //on a plural field, 'src' is where it should look to know whether it's plural or not
        if (arg.src === "OUTPUT") {
          connection = block.outputConnection;
          if (block.outputConnection.targetBlock()) {
            let targetBlock = connection.targetBlock();
            if (!targetBlock) {
              continue;
            }
            if (targetBlock.type.endsWith("_I") && targetBlock.getPreviousBlock()) {
              let prevBlock = targetBlock.getPreviousBlock();
              while (prevBlock != null && prevBlock.getPreviousBlock()) {
                prevBlock = prevBlock.getPreviousBlock();
              }
              connection = prevBlock?.outputConnection;
              targetBlock = connection?.targetBlock() ?? null;
            }
            if (targetBlock) {
              //if the 'src' arg appears on the 'input_value' it's connected to, redirect to that
              let name = targetBlock.getInputWithBlock(block)?.name;
              for (let arg of argsList1(targetBlock.json, "input")) {
                if (arg.name === name && arg.src) {
                  //
                  connection = targetBlock.getInput(arg.src)?.connection;
                }
              }
            }
          }
        } else {
          connection = block.getInput(arg.src!)?.connection;
        }
        if (!connection) {
          if (block.getField(arg.src!)) {
            shouldBePlural = block.getFieldValue(arg.src!) !== 1;
          }
        } else if (connection.targetBlock()) {
          let targetBlock = connection.targetBlock();
          if (targetBlock?.json?.plural != null) {
            shouldBePlural = targetBlock.json.plural;
          } else if (targetBlock?.type === "ValueProvider_int") {
            shouldBePlural = targetBlock.getFieldValue("int") !== 1;
          }
        }

        let before = block.getFieldValue(arg.name!);
        const options = arg.text!.split("/");
        if (shouldBePlural === null) {
          if (arg.value) {
            //on a plural field, 'value' is the default text to show (e.g. in the toolbox)
            block.setFieldValue(arg.value, <string>arg.name);
          } else {
            block.setFieldValue(arg.text, <string>arg.name);
          }
        } else if (shouldBePlural) {
          block.setFieldValue(options[1], <string>arg.name);
        } else {
          block.setFieldValue(options[0], <string>arg.name);
        }

        if (block.getFieldValue(arg.name!) !== before) {
          anyChange = true;
        }
      }
    }
  }

  if (anyChange) {
    for (let block of workspace.getAllBlocks(true)) {
      for (var i = 0, input; (input = block.inputList[i]); i++) {
        for (var j = 0, field: Field | FieldInputWithPrivateEditor; (field = input.fieldRow[j]); j++) {
          if ("isBeingEdited_" in field && "showEditor_" in field && typeof field["showEditor_"] === "function") {
            field["showEditor_"]();
          }
        }
      }
    }
  }
}

export function isSpellsourceBlock(type: string): boolean {
  return !!Blockly.Blocks[type]?.json?.path;
}

export function initArtBlcks(data: ContextType<typeof BlocklyDataContext>) {
  for (let art of data.allArt) {
    let type = "Art_" + art.name;
    if (type in Blockly.Blocks) {
      return;
    }
    const block: BlockDef & {
      art?: typeof art;
    } = addBlock({
      type: type,
      message0: "%1",
      args0: [
        {
          type: "field_image",
          width: art.width * 1.5,
          height: art.height * 1.5,
          src: art.src,
        },
      ],
      output: "Art",
      colour: "#A6A6A6",
      data: art.name,
      comment: art.name,
    } as BlockDef);
    block["art"] = art;
  }
}

export const refreshBlock = (block: BlockSvg | Block) => {
  block.data = Blockly.Blocks[block.type].data;

  if (block.type === "Card_REFERENCE") {
  } else if (block.type === "HeroClass_REFERENCE") {
    const color = heroClassColors[block.getFieldValue("id")];
    if (color && block.getColour() !== color) {
      block.setColour(color);
    }
  } else if (block.getField("message")) {
    block.setFieldValue(Blockly.Blocks[block.type].message, "message");
  }

  if ("render" in block) {
    let textElement = block.getSvgRoot().querySelector("text");
    const typeTextColor = textColor[block.type];
    const idTextColor = textColor[block.getFieldValue("id")];
    const color = typeTextColor ?? idTextColor;
    if (textElement && color) {
      textElement.style.fill = color;
    }
    block.render(false);
  }
};
