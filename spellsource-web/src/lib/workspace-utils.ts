import Blockly, { Block, Workspace } from "blockly";
import { isArray, isEmpty, isPlainObject, merge } from "lodash";
import format from "string-format";
import { RgbColour } from "../components/blockly/field-colour-hsv-sliders";

export const isNumeric = (str: any) => !isNaN(str) && !isNaN(parseFloat(str));

export const numberIfNumeric = (str: string) => (isNumeric(str) ? Number(str) : str);

export function blockStateToCardScript(block: Blockly.serialization.blocks.State) {
  let cardScript = {} as any;
  for (let [name, value] of Object.entries(block.fields ?? {})) {
    cardScript[name] = numberIfNumeric(value);
  }

  for (let [name, input] of Object.entries(block.inputs ?? {})) {
    const childBlock = input.block ?? input.shadow;
    if (!childBlock) {
      continue;
    }
    cardScript[name] = blockStateToCardScript(childBlock);
  }

  if ("customArg" in cardScript && "customValue" in cardScript) {
    cardScript[cardScript.customArg] = cardScript.customValue;
    delete cardScript.customArg;
    delete cardScript.customValue;
  }

  if (block.data && !(typeof block.data === "string" && block.data.startsWith("BLOCKLY_"))) {
    return block.data === "null" ? null : numberIfNumeric(format(block.data, cardScript));
  }

  if (block.data === "BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE" && cardScript.attribute) {
    cardScript = {
      [cardScript.attribute]: true,
    };
  }

  if (block.data === "BLOCKLY_INT_ATTRIBUTE" && cardScript.attribute) {
    cardScript = {
      [cardScript.attribute]: cardScript.value,
    };
  }

  if (block.data === "BLOCKLY_ARRAY_ELEMENT") {
    cardScript = cardScript.i ? [cardScript.i] : [cardScript];
  }

  if (block.data === "BLOCKLY_ARRAY") {
    cardScript = Object.values(cardScript);
  }

  if (block.data === "BLOCKLY_DICTIONARY") {
    for (let i = 0, key, value; (key = cardScript["key" + i]) && (value = cardScript["value" + i]); i++) {
      cardScript[key] = value;
      delete cardScript["key" + i];
      delete cardScript["value" + i];
    }
  }

  const nextBlock = block.next?.block ?? block.next?.shadow;
  if (nextBlock) {
    const nextCardScript = blockStateToCardScript(nextBlock);

    if (
      nextBlock.data === "BLOCKLY_EXTEND_PREVIOUS" ||
      nextBlock.data === "BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE" ||
      nextBlock.data === "BLOCKLY_INT_ATTRIBUTE"
    ) {
      merge(cardScript, nextCardScript);
    } else if (nextBlock.data === "BLOCKLY_ARRAY_ELEMENT") {
      if (!isArray(cardScript)) {
        cardScript = [cardScript];
      }
      cardScript.push(...(isArray(nextCardScript) ? nextCardScript : [nextCardScript]));
    } else {
      // ignored
    }
  }

  return postProcessCardScript(cardScript);
}

/**
 * Makes final changes to the cardScript to make it valid
 *
 * Input value names that contain '.'s will be rearranged,
 * as defined by the rearrangeInputValues method
 *
 * Cards that have opener(battlecry) and/or aftermath(deathrattle)
 * properties will be given their respective attributes
 *
 * Boolean values are also fixed here
 *
 * @param cardScript
 * @returns the modified cardScript
 */
function postProcessCardScript(cardScript: any) {
  if (isArray(cardScript)) {
    for (const cardScriptElement of cardScript) {
      postProcessCardScript(cardScriptElement);
    }
    return cardScript;
  }
  rearrangeInputValues(cardScript);
  if (cardScript.card && !(typeof cardScript.card === "string")) {
    delete cardScript.card;
  }
  if (cardScript.target === "IT") {
    delete cardScript.target;
  }
  if (cardScript.secondaryTarget === "IT") {
    delete cardScript.secondaryTarget;
  }
  if (cardScript.cardType === "ANY") {
    delete cardScript.cardType;
  }

  if (cardScript.hasOwnProperty("type") && cardScript.battlecry) {
    if (!cardScript.attributes) {
      cardScript.attributes = {};
    }
    cardScript.attributes.BATTLECRY = true;
  }

  if (cardScript.hasOwnProperty("type") && cardScript.deathrattle) {
    if (!cardScript.attributes) {
      cardScript.attributes = {};
    }
    cardScript.attributes.DEATHRATTLES = true;
  }

  if (cardScript.hasOwnProperty("type") && JSON.stringify(cardScript).includes(`"class":"DiscoverSpell"`)) {
    if (!cardScript.attributes) {
      cardScript.attributes = {};
    }
    cardScript.attributes.DISCOVER = true;
  }

  if (cardScript.class && cardScript.class.endsWith("Aura")) {
    if (
      cardScript.attribute &&
      !cardScript.attribute.startsWith("AURA_") &&
      !cardScript.attribute.startsWith("RESERVED") &&
      cardScript.attribute !== "SPELLS_CAST_TWICE"
    ) {
      cardScript.attribute = "AURA_" + cardScript.attribute;
    }

    if (cardScript.trigger) {
      cardScript.triggers = [cardScript.trigger];
      delete cardScript.trigger;
    }
  } else {
    if (cardScript.triggers && cardScript.triggers.length === 1) {
      cardScript.trigger = cardScript.triggers[0];
      delete cardScript.triggers;
    }
  }

  if (cardScript.aura && isArray(cardScript.aura)) {
    cardScript.aura = cardScript.aura[0];
  }

  if (cardScript.hasOwnProperty("colour")) {
    const color = new RgbColour().loadFromHex(cardScript.colour);
    delete cardScript.colour;
    for (let arg of ["r", "g", "b", "a"]) {
      const arg1 = arg as keyof RgbColour;
      cardScript[arg1] = Math.round(1000 * (color[arg1] as number)) / 1000;
    }
  }

  if (cardScript.class === "AlwaysCondition") {
    cardScript.class = "AndCondition";
  }
  if (cardScript.class === "NeverCondition") {
    cardScript.class = "OrCondition";
  }

  if (cardScript.type === "CLASS" && cardScript.id && !cardScript.heroClass) {
    cardScript.heroClass = cardScript.id;
  }

  if (cardScript.hasOwnProperty("type") && cardScript.hasOwnProperty("attributes")) {
    if (cardScript.attributes.BASE_ATTACK) {
      cardScript.baseAttack = cardScript.attributes.BASE_ATTACK;
      delete cardScript.attributes.BASE_ATTACK;
    }

    if (cardScript.attributes.BASE_HP) {
      cardScript.baseHp = cardScript.attributes.BASE_HP;
      delete cardScript.attributes.BASE_HP;
    }
  }

  return cardScript;
}

/**
 * Usage:
 *    ...
 *    {
 *      "super.X": "value"
 *      ...
 *    }
 *
 *    super tries to move the argument up a level,
 *    so that the level above will look like
 *
 *    ...
 *    "X": "value",
 *    {
 *      ...
 *    }
 *
 *    -----------------------------------------------------------------------
 *
 *    {
 *      ...
 *      "X.Y": "value"
 *    }
 *
 *    other uses of '.' try to move the argument down a level,
 *    so that it will look like
 *
 *    {
 *      ...
 *      "X": {
 *        "Y": "value"
 *      }
 *    }
 *
 *    if "X" is already present, then "Y" will simply be put in as an argument
 *    if "X" isn't there already, it will be created
 *
 *    -----------------------------------------------------------------------
 *
 *    ...
 *    "X,Y.Z": "value"
 *    ...
 *
 *    ',' will put a value into multiple different places,
 *    so that it will look like
 *
 *    ...
 *    "X": "value",
 *    "Y.Z": "value"
 *    ...
 *
 *    which will be split as shown above, turning into
 *
 *    ...
 *    "X": "value",
 *    "Y": {
 *      "Z": "value"
 *    }
 * @param cardScript
 */
function rearrangeInputValues(cardScript: any) {
  if (typeof cardScript === "string") {
    return;
  }

  //first, split up any args with ','
  for (const cardScriptKey in cardScript) {
    if (cardScriptKey.includes(",")) {
      let newKeys = cardScriptKey.split(",");
      for (const key of newKeys) {
        cardScript[key] = cardScript[cardScriptKey];
      }
      delete cardScript[cardScriptKey];
    }
  }

  //go through the children to bring super.* up
  for (const cardScriptKey in cardScript) {
    if (cardScript.propertyIsEnumerable(cardScriptKey)) {
      //first time go through all the ones that definitely won't override what we're working with
      for (const cardScriptElementKey in cardScript[cardScriptKey]) {
        if (cardScriptElementKey.startsWith("super.")) {
          let newKey = cardScriptElementKey.substring(cardScriptElementKey.indexOf(".") + 1);
          if (cardScriptKey.includes(".")) {
            let correctPrefix = cardScriptKey.split(".").slice(0, -1).join(".");
            cardScript[correctPrefix + "." + newKey] = cardScript[cardScriptKey][cardScriptElementKey];
          } else if (cardScriptKey === "super") {
            cardScript[cardScriptElementKey] = cardScript[cardScriptKey][cardScriptElementKey];
          } else {
            cardScript[newKey] = cardScript[cardScriptKey][cardScriptElementKey];
          }
          delete cardScript[cardScriptKey][cardScriptElementKey];
        }
      }
      //then do the last one that might override what we're working with
      if (cardScript[cardScriptKey]["super"] && typeof cardScript[cardScriptKey]["super"] === "string") {
        cardScript[cardScriptKey] = cardScript[cardScriptKey]["super"];
      } else if (
        cardScript["super"] &&
        cardScript.propertyIsEnumerable("super") &&
        typeof cardScript["super"] !== "string"
      ) {
        let andWhenEveryonesSuper = cardScript.super.super;
        merge(cardScript, cardScript["super"]);
        if (andWhenEveryonesSuper) {
          //no one will be
        } else {
          delete cardScript["super"];
        }
      }
    }
  }

  //go through the keys here to bring down any *.*
  for (const cardScriptKey in cardScript) {
    if (!cardScriptKey.startsWith("super") && cardScriptKey.includes(".")) {
      let newKey = cardScriptKey.substring(0, cardScriptKey.indexOf("."));
      let newKey2 = cardScriptKey.substring(cardScriptKey.indexOf(".") + 1);
      if (!cardScript.hasOwnProperty(newKey)) {
        cardScript[newKey] = {};
      }
      if (cardScript.propertyIsEnumerable(newKey)) {
        cardScript[newKey][newKey2] = cardScript[cardScriptKey];
        delete cardScript[cardScriptKey];
        postProcessCardScript(cardScript[newKey]);
      }
    }

    //gotta do this because it seems like the original block -> xml conversion hates booleans
    if (cardScript[cardScriptKey] === "TRUE") {
      cardScript[cardScriptKey] = true;
    }
    if (cardScript[cardScriptKey] === "FALSE") {
      cardScript[cardScriptKey] = false;
    }
    if (isPlainObject(cardScript[cardScriptKey]) && isEmpty(cardScript[cardScriptKey])) {
      delete cardScript[cardScriptKey];
    }
    if (cardScript[cardScriptKey] === "SHADOW") {
      delete cardScript[cardScriptKey];
    }
  }
}

export function workspaceToCardScript(workspace: Workspace) {
  const state = Blockly.serialization.workspaces.save(workspace);

  const blocks = state.blocks.blocks as Blockly.serialization.blocks.State[];

  if (blocks.length === 1) {
    return blockStateToCardScript(blocks[0]);
  } else {
    return blocks.map(blockStateToCardScript);
  }
}

export function blockToCardScript(block: Block) {
  const state = Blockly.serialization.blocks.save(block)!;
  return blockStateToCardScript(state);
}
