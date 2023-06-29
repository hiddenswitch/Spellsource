import Blockly, { Block, BlockSvg, Connection, Workspace, WorkspaceSvg } from "blockly";
import * as BlocklyMiscUtils from "./blockly-misc-utils";
import { isArray } from "lodash";
import { BlockArgDef, BlockDef } from "../__generated__/blocks";
import { CardDef } from "../components/card-display";
import { isNumeric } from "./workspace-utils";

const classBlocksDictionary = {}; //A dictionary mapping the 'class' argument a block uses to the block itself
const enumBlocksDictionary = {}; //A dictionary mapping the enum value of the block to the block itself
const allArgNames = new Set<string>(); //Every different possible arg name that appears on blocks, (for searching)

export const blockTypeColors = {};

export const errorOnCustom = false;

const customBlocks = {};

/**
 * Creates a reference for the block's json that's easily accessible by
 * either its output or 'class' argument
 * @param block The block to add
 */
export function addBlockToMap(block: BlockDef) {
  if (block.type.endsWith("SHADOW")) {
    return;
  }
  let list = argsList(block).filter((arg) => arg.type !== "field_image");
  if (list.length > 0) {
    let className = null;

    for (let arg of list) {
      if (!arg.name) {
        continue;
      }
      allArgNames.add(arg.name);
      if (arg.name.endsWith("class")) {
        className = arg.value;
        break;
      }
    }
    if (!!className) {
      if (!classBlocksDictionary[className]) {
        classBlocksDictionary[className] = [];
      }
      classBlocksDictionary[className].push(block);
    }
  } else {
    if (!enumBlocksDictionary[block.data]) {
      enumBlocksDictionary[block.data] = [];
    }
    enumBlocksDictionary[block.data].push(block);
  }
}

/**
 * OVERALL METHOD TO CREATE THE CARD ON THE WORKSPACE
 * @param workspace The workspace
 * @param card The card json to generate from
 * @returns The created starter block
 */
export function generateCard(workspace: Workspace | WorkspaceSvg, card: CardDef) {
  let type = card.type as string;
  if (!!card.quest) {
    type = "QUEST";
  } else if (!!card.secret) {
    type = "SECRET";
  } else if (type === "HERO" && !card.attributes.hasOwnProperty("HP")) {
    type = "HERO2";
  }
  let block = BlocklyMiscUtils.newBlock(workspace, "Starter_" + type);
  let args = ["baseManaCost", "name", "baseAttack", "baseHp", "description", "countUntilCast", "damage", "durability"];
  args.forEach((arg) => {
    if (!!card[arg] && !!block.getField(arg)) {
      block.setFieldValue(card[arg], arg);
    }
  });

  if ("initSvg" in block) {
    block.initSvg();
  }
  if (!!card.attributes && !!card.attributes.HP) {
    block.setFieldValue(card.attributes.HP, "attributes.HP,attributes.MAX_HP");
  }

  if (!!block.getInput("name")) {
    block.getInput("name").connection.targetBlock().setFieldValue(card.name, "text");
  }
  if (!!block.getInput("description")) {
    block.getInput("description").connection.targetBlock().setFieldValue(card.description, "text");
  }

  let lowestBlock = block;

  for (let arg of ["heroClass", "rarity", "spell", "targetSelection", "secret", "quest", "heroPower", "hero"]) {
    if (card.type === "CLASS" && arg === "heroClass") {
      block.getInput("heroClass").connection.targetBlock().setFieldValue(card.heroClass, "text");
    } else if (card.type === "HERO_POWER" && arg === "spell") {
      if (card.spell["class"] == "HeroPowerSpell") {
        handleArg(block.getInput("spell.spell").connection, card.spell["spell"], "spell.spell", workspace, card.spell);
      } else {
        handleArg(block.getInput("spell.spell").connection, card.spell, "spell.spell", workspace, card.spell);
      }
    } else if (card.hasOwnProperty(arg) && !!block.getInput(arg)) {
      simpleHandleArg(block, arg, card, workspace);
    }
  }

  if (!!card.race && card.type === "MINION") {
    simpleHandleArg(block, "race", card, workspace);
  }

  if (!!card.countByValue && (String(card.countByValue) === "TRUE" || card.countByValue === true)) {
    block.setFieldValue("TRUE", "countByValue");
  }

  if (!!card.battlecry) {
    let openerBlock;
    if (!!card.battlecry.condition) {
      openerBlock = BlocklyMiscUtils.newBlock(workspace, "Property_opener2");
    } else {
      openerBlock = BlocklyMiscUtils.newBlock(workspace, "Property_opener1");
    }
    lowestBlock.nextConnection.connect(openerBlock.previousConnection);
    if ("initSvg" in openerBlock) {
      openerBlock.initSvg();
    }

    handleArg(
      openerBlock.getInput("battlecry.targetSelection").connection,
      card.battlecry.targetSelection,
      "battlecry.targetSelection",
      workspace,
      card.battlecry
    );
    handleArg(
      openerBlock.getInput("battlecry.spell").connection,
      card.battlecry.spell,
      "battlecry.spell",
      workspace,
      card.battlecry
    );

    if (!!card.battlecry.condition) {
      handleArg(
        openerBlock.getInput("battlecry.condition").connection,
        card.battlecry.condition,
        "battlecry.condition",
        workspace,
        card.battlecry
      );
    }
    lowestBlock = openerBlock;
  }

  if (!!card.deathrattle) {
    let aftermathBlock = BlocklyMiscUtils.newBlock(workspace, "Property_aftermath");
    lowestBlock.nextConnection.connect(aftermathBlock.previousConnection);
    if ("initSvg" in aftermathBlock) {
      aftermathBlock.initSvg();
    }
    simpleHandleArg(aftermathBlock, "deathrattle", card, workspace);
    lowestBlock = aftermathBlock;
  }

  const triggers = (trigger, property) => {
    if (!!card[trigger + "s"] || !!card[trigger]) {
      let triggersBlock = BlocklyMiscUtils.newBlock(workspace, property);
      lowestBlock.nextConnection.connect(triggersBlock.previousConnection);
      if ("initSvg" in triggersBlock) {
        triggersBlock.initSvg();
      }
      let triggers;
      if (!!card[trigger]) {
        triggers = [card[trigger]];
      } else {
        triggers = card[trigger + "s"];
      }
      let lowestConnection = triggersBlock.getFirstStatementConnection();
      for (let trigger of triggers) {
        let triggerBlock = enchantment(trigger, workspace);
        lowestConnection.connect(triggerBlock.previousConnection);
        lowestConnection = triggerBlock.nextConnection;
        if ("initSvg" in triggerBlock) {
          triggerBlock.initSvg();
        }
      }
      lowestBlock = triggersBlock;
    }
  };
  triggers("trigger", "Property_triggers");
  triggers("passiveTrigger", "Property_triggers2");
  triggers("deckTrigger", "Property_triggers3");
  triggers("gameTrigger", "Property_triggers4");

  if (!!card.auras || !!card.aura) {
    let aurasBlock = BlocklyMiscUtils.newBlock(workspace, "Property_auras");
    lowestBlock.nextConnection.connect(aurasBlock.previousConnection);
    if ("initSvg" in aurasBlock) {
      aurasBlock.initSvg();
    }
    auras(aurasBlock, card, workspace);
    lowestBlock = aurasBlock;
  }

  if (!!card.attributes) {
    delete card.attributes.SPELLSOURCE_NAME;
    delete card.attributes.BATTLECRY;
    delete card.attributes.DEATHRATTLES;
    delete card.attributes.DISCOVER;
    if (card.type === "HERO") {
      delete card.attributes.HP;
      delete card.attributes.MAX_HP;
    }
    if (Object.values(card.attributes).length > 0) {
      let attributesBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes");
      lowestBlock.nextConnection.connect(attributesBlock.previousConnection);
      if ("initSvg" in attributesBlock) {
        attributesBlock.initSvg();
      }
      let lowestConnection = attributesBlock.getFirstStatementConnection();
      for (let atr in card.attributes) {
        let attributeBlock;
        if (isNumeric(card.attributes[atr])) {
          attributeBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes_int");
          attributeBlock.getField("value").setValue(card.attributes[atr]);
        } else {
          attributeBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes_boolean");
        }
        handleArg(attributeBlock.getInput("attribute").connection, atr, "attribute", workspace, card.attributes);
        if ("initSvg" in attributeBlock) {
          attributeBlock.initSvg();
        }
        lowestConnection.connect(attributeBlock.previousConnection);
        lowestConnection = attributeBlock.nextConnection;
      }

      lowestBlock = attributesBlock;
    }
  }

  if (!!card.manaCostModifier) {
    let costyBlock = null;
    if (card.manaCostModifier["class"] === "ConditionalValueProvider" && card.manaCostModifier["ifFalse"] === 0) {
      costyBlock = BlocklyMiscUtils.newBlock(workspace, "Property_manaCostModifierConditional");
      handleArg(
        costyBlock.getInput("manaCostModifier.condition").connection,
        card.manaCostModifier["condition"],
        "condition",
        workspace,
        card
      );
      if (typeof card.manaCostModifier["ifTrue"] === "object") {
        handleArg(
          costyBlock.getInput("manaCostModifier.ifTrue").connection,
          card.manaCostModifier["ifTrue"],
          "ifTrue",
          workspace,
          card
        );
      } else {
        handleIntArg(costyBlock, costyBlock.json.args0[0].name, workspace, card.manaCostModifier["ifTrue"]);
      }
    } else {
      costyBlock = BlocklyMiscUtils.newBlock(workspace, "Property_manaCostModifier");
      handleArg(
        costyBlock.getInput("manaCostModifier").connection,
        card.manaCostModifier,
        "manaCostModifier",
        workspace,
        card
      );
    }
    lowestBlock.nextConnection.connect(costyBlock.previousConnection);
    if ("initSvg" in costyBlock) {
      costyBlock.initSvg();
    }
    lowestBlock = costyBlock;
  }

  if (!!card.cardCostModifier) {
    let costyBlock = BlocklyMiscUtils.newBlock(workspace, "Property_cardCostModifier");
    lowestBlock.nextConnection.connect(costyBlock.previousConnection);
    if ("initSvg" in costyBlock) {
      costyBlock.initSvg();
    }

    costModifier(costyBlock, card.cardCostModifier, workspace);

    lowestBlock = costyBlock;
  }

  if (!!card.dynamicDescription) {
    let descriptionsBlock = BlocklyMiscUtils.newBlock(workspace, "Property_descriptions");

    dynamicDescription(workspace, descriptionsBlock.getFirstStatementConnection(), card.dynamicDescription, "i");

    descriptionsBlock.previousConnection.connect(lowestBlock.nextConnection);
    if ("initSvg" in descriptionsBlock) {
      descriptionsBlock.initSvg();
    }
    lowestBlock = descriptionsBlock;
  }

  if (card.set !== "CUSTOM") {
    let setBlock = BlocklyMiscUtils.newBlock(workspace, "Property_set");
    setBlock.setFieldValue(card.set, "set");
    setBlock.previousConnection.connect(lowestBlock.nextConnection);
    if ("initSvg" in setBlock) {
      setBlock.initSvg();
    }
    lowestBlock = setBlock;
  }

  if (!!card.condition) {
    let conditionBlock = BlocklyMiscUtils.newBlock(workspace, "Property_condition");
    simpleHandleArg(conditionBlock, "condition", card, workspace);
    conditionBlock.previousConnection.connect(lowestBlock.nextConnection);
    if ("initSvg" in conditionBlock) {
      conditionBlock.initSvg();
    }
    lowestBlock = conditionBlock;
  }

  if ((card.collectible === false || String(card.collectible) === "FALSE") && type !== "HERO") {
    let uncollectibleBlock = BlocklyMiscUtils.newBlock(workspace, "Property_uncollectible");
    uncollectibleBlock.previousConnection.connect(lowestBlock.nextConnection);
    if ("initSvg" in uncollectibleBlock) {
      uncollectibleBlock.initSvg();
    }
    lowestBlock = uncollectibleBlock;
  }

  if (!!card.art) {
    for (let path of ["art.primary", "art.secondary", "art.shadow", "art.highlight", "art.body.vertex"]) {
      let json = card;
      for (let arg of path.split(".")) {
        if (!!json) {
          json = json[arg];
        }
      }

      if (!!json && !!block.getInput(path)) {
        let colorBlock = block.getInput(path).connection.targetBlock();
        for (let i of ["r", "g", "b", "a"]) {
          colorBlock.setFieldValue(Math.round(json[i] * 255), i);
        }
      }
    }
    if (!!card.art["glow"]) {
      let glowBlock = BlocklyMiscUtils.newBlock(workspace, "Property_glow");
      glowBlock.previousConnection.connect(lowestBlock.nextConnection);
      let colorBlock = glowBlock.getInput("art.glow").connection.targetBlock();
      for (let i of ["r", "g", "b", "a"]) {
        colorBlock.setFieldValue(Math.round(card.art["glow"][i] * 255), i);
      }
      if ("initSvg" in glowBlock) {
        glowBlock.initSvg();
      }
      lowestBlock = glowBlock;
    }
  }

  if (!!card.art?.sprite?.named) {
    let spriteBlock = BlocklyMiscUtils.newBlock(workspace, "Property_sprite");
    spriteBlock.previousConnection.connect(lowestBlock.nextConnection);

    if (!!Blockly.Blocks["Art_" + card.art.sprite.named]) {
      let artBlock = BlocklyMiscUtils.newBlock(workspace, "Art_" + card.art.sprite.named);
      spriteBlock.getInput("art.sprite.named").connection.connect(artBlock.outputConnection);
      if ("initSvg" in artBlock) {
        artBlock.initSvg();
      }
    }

    if ("initSvg" in spriteBlock) {
      spriteBlock.initSvg();
    }
    lowestBlock = spriteBlock;
  }

  if ("render" in workspace) {
    workspace.render();
  }

  return block;
}

/**
 * Copies over the dynamic description fields for a card
 *
 * @param workspace The workspace
 * @param connection The statement connection for the list of descriptions
 * @param descriptions The json array of descriptions
 * @param inputName The name of the descriptions input argument on the block
 */
export function dynamicDescription(workspace: Workspace, connection: Connection, descriptions, inputName) {
  for (let dynamicDescription of descriptions) {
    let block = connection.targetBlock() as Block | BlockSvg;
    if (!block) {
      block = BlocklyMiscUtils.newBlock(workspace, "Property_description");
      connection.connect(block.previousConnection);
      if ("initSvg" in block) {
        block.initSvg();
      }
    }
    if (typeof dynamicDescription === "string") {
      dynamicDescription = {
        class: "StringDescription",
        string: dynamicDescription,
      };
    }

    let descBlock = BlocklyMiscUtils.newBlock(workspace, "Property_" + dynamicDescription.class);

    if (descBlock === null) {
      continue;
    }

    block.getInput(inputName).connection.connect(descBlock.outputConnection);

    if (!!dynamicDescription.value) {
      if (isNumeric(dynamicDescription.value)) {
        handleIntArg(descBlock, "value", workspace, dynamicDescription.value);
      } else {
        handleArg(
          descBlock.getInput("value").connection,
          dynamicDescription.value,
          "value",
          workspace,
          dynamicDescription
        );
      }
    }
    if (!!dynamicDescription.condition) {
      handleArg(
        descBlock.getInput("condition").connection,
        dynamicDescription.condition,
        "condition",
        workspace,
        dynamicDescription
      );
    }
    if (!!dynamicDescription.string) {
      descBlock.setFieldValue(dynamicDescription.string, "string");
    }
    if (dynamicDescription.hasOwnProperty("description1")) {
      dynamicDescription(
        workspace,
        block.getInput(inputName).connection,
        [dynamicDescription.description1],
        "description1"
      );
    }
    if (dynamicDescription.hasOwnProperty("description2")) {
      dynamicDescription(
        workspace,
        block.getInput(inputName).connection,
        [dynamicDescription.description2],
        "description2"
      );
    }

    if (!!dynamicDescription.descriptions) {
      dynamicDescription(workspace, descBlock.getFirstStatementConnection(), dynamicDescription.descriptions, "i");
    }

    block.getInput(inputName).connection.connect(descBlock.outputConnection);
    connection = block.nextConnection;
    if ("initSvg" in descBlock) {
      descBlock.initSvg();
    }
  }
}

/**
 * Handles finding an input on a block by what its name ends with
 * @param block The block (not its json definition) to search through
 * @param inputName What the name has to end with
 * @returns The json for the correct input, or null
 */
export function getInputEndsWith(block, inputName) {
  for (let name of allArgNames) {
    let input = block.getInput(name);
    if (!!input) {
      if (name.endsWith(inputName)) {
        return input;
      }
    }
  }
  return null;
}

/**
 * Whether we need to use the EnchantmentOptions block and not just the Enchantment block
 *
 * Used to be more complicated, but the trigger conditions got streamlined
 * @param trigger
 * @param props
 * @returns {boolean}
 */
export function enchantmentNeedsOptions(trigger, props) {
  return !(props.length === 2 && !!trigger.eventTrigger && !!trigger.spell);
}

/**
 * Enchantments are special enough to need their own method
 * because of the unique option blocks
 *
 * @param trigger The json of the trigger to be blockified
 * @param workspace The workspace
 * @param triggerBlock The enchantment block that may or may not already be
 * present in the list of enchantment statements
 * @returns The created block
 */
export function enchantment(trigger, workspace, triggerBlock: Block | BlockSvg = null) {
  let props = relevantProperties(trigger);
  if (!enchantmentNeedsOptions(trigger, props)) {
    if (!triggerBlock) {
      triggerBlock = BlocklyMiscUtils.newBlock(workspace, "Enchantment");
    }
  } else {
    if (!triggerBlock) {
      triggerBlock = BlocklyMiscUtils.newBlock(workspace, "EnchantmentOptions");
    }
    let lowestOptionConnection = triggerBlock.getFirstStatementConnection();
    for (let prop of props) {
      if (prop === "spell" || prop === "eventTrigger") {
        continue;
      }
      let match = null;
      for (let blockType in Blockly.Blocks) {
        let block = Blockly.Blocks[blockType].json;
        if (block?.type?.startsWith("EnchantmentOption")) {
          for (let arg of argsList(block)) {
            if (arg.name === prop) {
              match = block;
            }
          }
        }
      }
      if (!match) {
        console.warn(`Failed to handle prop ${prop} on trigger`, trigger);
        continue;
      }
      let option = BlocklyMiscUtils.newBlock(workspace, match.type);
      if (trigger[prop] !== true) {
        option.setFieldValue(trigger[prop], prop);
      }
      if ("initSvg" in option) {
        option.initSvg();
      }
      lowestOptionConnection.connect(option.previousConnection);
      lowestOptionConnection = lowestOptionConnection.targetBlock().nextConnection;
    }
  }
  handleArg(triggerBlock.getInput("spell").connection, trigger.spell, "spell", workspace, trigger);
  handleArg(triggerBlock.getInput("eventTrigger").connection, trigger.eventTrigger, "eventTrigger", workspace, trigger);

  return triggerBlock;
}

/**
 * Whether we need to use the EnchantmentOptions block and not just the Enchantment block
 *
 * Used to be more complicated, but the trigger conditions got streamlined
 * @param costModifier
 * @param props
 * @returns {boolean}
 */
export function costModifierNeedsOptions(costModifier, props) {
  if (costModifier.class === "OneTurnCostModifier") {
    return true;
  }
  for (let prop of props) {
    if (!!Blockly.Blocks["CostModifierOption_" + prop]) {
      return true;
    }
  }
  return false;
}

/**
 * Cost Modifiers are special enough to need their own method
 * because of the unique option blocks
 *
 * @param costyBlock
 * @param costModifier The json of the trigger to be blockified
 * @param workspace The workspace
 * @returns The created block
 */
export function costModifier(costyBlock: Block | BlockSvg, costModifier, workspace) {
  costModifier = mutateJson(costModifier);
  if (typeof costModifier.value !== "object" && costModifier.value < 0) {
    if (costModifier.operation === "SUBTRACT") {
      costModifier.operation = "ADD";
    } else {
      costModifier.operation = "SUBTRACT";
    }
    costModifier.value *= -1;
  }
  let props = relevantProperties(costModifier);
  let costModifierBlock;
  let lowestOptionConnection;
  if (!costModifierNeedsOptions(costModifier, props)) {
    costModifierBlock = BlocklyMiscUtils.newBlock(workspace, "CostModifier");
  } else {
    costModifierBlock = BlocklyMiscUtils.newBlock(workspace, "CostModifierOptions");
    lowestOptionConnection = costModifierBlock.getFirstStatementConnection();
    for (let prop of props) {
      if (prop === "value" || prop === "operation" || prop === "target" || prop === "filter") {
        continue;
      }
      let option = BlocklyMiscUtils.newBlock(workspace, "CostModifierOption_" + prop);
      handleInputs(Blockly.Blocks["CostModifierOption_" + prop].json, costModifier, option, workspace, null);
      if ("initSvg" in option) {
        option.initSvg();
      }
      lowestOptionConnection.connect(option.previousConnection);
      lowestOptionConnection = lowestOptionConnection.targetBlock().nextConnection;
    }
  }
  if (costModifier.class === "OneTurnCostModifier") {
    let option = BlocklyMiscUtils.newBlock(workspace, "CostModifierOption_oneTurn");
    if ("initSvg" in option) {
      option.initSvg();
    }
    lowestOptionConnection.connect(option.previousConnection);
  }
  if (typeof costModifier.value !== "object") {
    handleIntArg(costModifierBlock, "value", workspace, costModifier.value);
  } else {
    simpleHandleArg(costModifierBlock, "value", costModifier, workspace);
  }
  if (!!costModifier.target) {
    simpleHandleArg(costModifierBlock, "target", costModifier, workspace);
  }
  if (!!costModifier.operation) {
    costModifierBlock.setFieldValue(costModifier.operation, "operation");
  }

  if (costModifierBlock.initSvg) {
    costModifierBlock.initSvg();
  }

  costyBlock.getInput("cardCostModifier").connection.connect(costModifierBlock.outputConnection);
}

/**
 * Auras are also weird enough to need their own method,
 * since they're the only typical 'Desc' style json object
 * that's statement style and not value style
 *
 * Functions both for json.aura and json.auras
 *
 * @param block The block that has the aura statement input
 * @param json The json of the aura / auras
 * @param workspace The workspace
 */
export function auras(block: Block | BlockSvg, json, workspace) {
  let auras;
  if (!!json.aura) {
    auras = [json.aura];
  } else {
    auras = json["auras"];
  }
  let lowestConnection = block.getFirstStatementConnection();
  for (let aura of auras) {
    handleArg(lowestConnection, aura, "aura", workspace, aura, true);
    lowestConnection = lowestConnection.targetBlock().nextConnection;
  }
}

/**
 * Finds the best block match for a given bit of json
 *
 * For Enums/Strings, it just simply finds the corresponding Block
 *
 * For actual JSON objects, the tl; dr is it finds the block that:
 *  - covers all the required json properties, determined by relevantProperties()
 *  - has a lot of arguments that the json does actually have
 *  - doesn't have too many arguments that the json doesn't have
 *
 * If no blocks match the first criteria, nothing is returned
 * The other criteria are for picking the best block to return
 *
 * @param json The json we need to find a match for
 * @param inputName The name of the argument that json is assigned to
 * @param parentJson The level of json above json
 * @returns The Block that's the best match (its JSON definition), or null if no good matches
 */
export function getMatch(json, inputName, parentJson) {
  let matches = null;
  let bestMatch = null;
  if (typeof json !== "object") {
    //just looking for the correct block with the data of the json string
    let lookingForType = BlocklyMiscUtils.inputNameToBlockType(inputName);
    if (inputName === "attribute") {
      json = json.toString().replace("AURA_", "");
    }
    matches = enumBlocksDictionary[json];
    if (!matches || matches.length === 0) {
      return;
    }
    for (let match of matches) {
      if (!lookingForType || match.type.startsWith(lookingForType) || match.type.startsWith("CatalogueCard")) {
        return match;
      }
    }
  } else if (!!json.class) {
    //need to find the block that represents that class
    let className = json.class;
    if (className === "AddEnchantmentSpell" && !!json.trigger) {
      if (enchantmentNeedsOptions(json.trigger, relevantProperties(json.trigger))) {
        if (!!json.revertTrigger) {
          return Blockly.Blocks["Spell_AddEnchantment5"].json;
        } else {
          return Blockly.Blocks["Spell_AddEnchantment2"].json;
        }
      } else {
        if (!!json.revertTrigger) {
          return Blockly.Blocks["Spell_AddEnchantment4"].json;
        } else {
          return Blockly.Blocks["Spell_AddEnchantment"].json;
        }
      }
    }
    if (className === "AddPactSpell") {
      return Blockly.Blocks["Spell_AddPact"].json;
    }
    if (className === "CardCostModifierSpell") {
      return Blockly.Blocks["Spell_CardCostModifier"].json;
    }
    if (className.endsWith("CostModifier")) {
      if (costModifierNeedsOptions(json, relevantProperties(json))) {
        return Blockly.Blocks["CostModifierOptions"].json;
      } else {
        return Blockly.Blocks["CostModifier"].json;
      }
    }
    matches = classBlocksDictionary[className];
    if (!matches || matches.length === 0) {
      return;
    }
    let relevantProps = relevantProperties(json);
    let goodMatches = [];
    for (let match of matches) {
      //for each possible match
      let hasAllProps = true;
      for (let property of relevantProps) {
        //check the json's relevant properties
        if ((relevantProps.includes("target") || match.output === "SpellDesc") && property === "filter") {
          continue;
        }
        if (
          (className.endsWith("Spell") || className.endsWith("ValueProvider") || className.endsWith("Source")) &&
          property === "targetPlayer"
        ) {
          continue;
        }
        if (className.endsWith("Trigger")) {
          if (json.targetPlayer === "BOTH" && property === "targetPlayer") {
            continue;
          }
          if (property === "race" || property === "requiredAttribute") {
            continue;
          }
        }

        let hasThisProp = false;
        for (let arg of argsList(match)) {
          //see if the match has a corresponding prop
          if (arg.type === "field_label_plural") {
            continue;
          }
          if (arg.name.split(".")[0] === property) {
            //just a surface level check, not traversing nested args
            if (arg.type === "field_label_serializable_hidden") {
              if ((arg.value === "TRUE" ? true : arg.value) === json[property]) {
                hasThisProp = true;
              }
            } else {
              hasThisProp = true;
            }
          }
        }
        if (!hasThisProp) {
          //if it doesn't, it's not good enough
          hasAllProps = false;
          break;
        }
      }
      if (hasAllProps) {
        //if it covers all the json's properties, it's good enough
        goodMatches.push(match);
      }
    }
    let bestScore = 0;
    for (let goodMatch of goodMatches) {
      //choose the one with the highest number of correct properties
      let bestScore = null;
      for (let goodMatch of matches) {
        let argList = argsList(goodMatch);
        let hasOneNormalArg = argList.length === 0;
        for (let arg of argList) {
          if (!arg.name.includes(".") && !arg.name.includes("super")) {
            hasOneNormalArg = true;
          }
        }
        if (!hasOneNormalArg) {
          continue;
        }
        let score = 0;
        for (let arg of argList) {
          if (arg.type === "field_label_plural") {
            continue;
          }
          let delta = 0;
          let property = traverseJsonByArgName(arg.name, json, parentJson);
          if (property !== null && property !== undefined) {
            if (arg.type === "field_label_serializable_hidden") {
              if ((arg.value === "TRUE" ? true : arg.value) === property) {
                delta = 2;
              } else {
                delta = -5; // an unchangeable field on the block is wrong... not a good look
              }
            } else {
              delta = 2; //if it's an input, we assume the correct block can be put here
            }
          } else {
            if (arg.type === "field_label_serializable_hidden") {
              delta = -5;
            } else {
              //it's kinda bad to straight up not have the property
              if (arg.name === "targetPlayer" || arg.name === "value") {
                delta = -0.5;
              } else {
                delta = -1;
              }
            }
          }
          score += delta;
        }
        if (score > bestScore || (score >= bestScore && bestMatch?.type.localeCompare(goodMatch.type) > 0)) {
          //for tied scores, do the one that comes alphabetically first
          //e.g. choosing ExampleSpell1 instead of ExampleSpell2
          bestMatch = goodMatch;
          bestScore = score;
        }
      }
    }
  } else {
    //what the heck could it even be if it doesn't have a class?
  }
  return bestMatch;
}

/**
 * Handles an argument on a block, recursively constructing the relevant blocks based on the json
 * @param connection The Blockly connection object where the new Block will have to go
 * @param json The json to determine the newBlock from
 * @param inputName The name of the argument that json is assigned to
 * @param workspace The workspace
 * @param parentJson The level of json above json
 * @param statement Whether we're connected a statement rather than an input
 */
export function handleArg(connection, json, inputName, workspace, parentJson, statement = false) {
  json = mutateJson(json);

  if (!!connection.targetBlock() && connection.targetBlock().type === "Property_text_SHADOW") {
    connection.targetBlock().setFieldValue(json, "text");
    return;
  }

  let bestMatch = getMatch(json, inputName, parentJson);
  let block;
  if (!bestMatch) {
    /*
    try {
      generateDummyBlock(json, inputName, parentJson)
    } catch (e) {
      //generating this quality of life dummy block is never worth crashing about
      console.log('Tried to generate a dummy block for ' + json + ' but failed because of ' + e)
    }

     */
    if (errorOnCustom) {
      throw Error("Couldn't generate without custom blocks");
    }
    block = handleNoMatch(json, inputName, parentJson, workspace);
  } else if (!!connection.targetBlock() && connection.targetBlock().type === bestMatch.type) {
    if (!connection.targetBlock().isShadow()) {
      block = connection.targetBlock();
      connection.disconnect();
      //just simpler to disconnect it and then reconnect it
    }
  }
  if (!block) {
    block = BlocklyMiscUtils.newBlock(workspace, bestMatch.type);
  }
  if ("initSvg" in block) {
    block.initSvg();
  }

  let outerBlock = wrapperBlocks(block, json, inputName, workspace, parentJson, connection, bestMatch);

  if (statement) {
    connection.connect(outerBlock.previousConnection);
  } else {
    connection.connect(outerBlock.outputConnection);
  }

  if (!bestMatch) {
    return; //args already taken care of by handleNoMatch
  }

  handleInputs(bestMatch, json, block, workspace, parentJson);
}

export function handleInputs(bestMatch, json, block: Block | BlockSvg, workspace, parentJson) {
  //now handle each dropdown on the new block (assumes stuff will just work)
  for (let dropdown of dropdownsList(bestMatch)) {
    let jsonElement = json[dropdown.name];
    if (
      !jsonElement &&
      (dropdown.name.includes(".") || dropdown.name.includes("super") || dropdown.name.includes(","))
    ) {
      jsonElement = traverseJsonByArgName(dropdown.name, json, parentJson);
    }

    if (!jsonElement) {
      continue;
    }

    block.setFieldValue(jsonElement, dropdown.name);
  }

  //now handle each input on the new block
  for (let inputArg of inputsList(bestMatch)) {
    let name = inputArg.name;
    let argName = name;
    let jsonElement = json[name];
    if (!jsonElement && (name.includes(".") || name.includes("super") || name.includes(","))) {
      jsonElement = traverseJsonByArgName(name, json, parentJson);
      name = name.split(".").slice(-1)[0];
    }

    if (jsonElement === null || jsonElement === undefined) {
      if (
        block.getInput(name)?.connection.targetBlock()?.type === "EntityReference_SHADOW" ||
        block.getInput(name)?.connection.targetBlock()?.type === "EntityReference_IT"
      ) {
        let it = BlocklyMiscUtils.newBlock(workspace, "EntityReference_IT");
        block.getInput(name).connection.connect(it.outputConnection);
        if ("initSvg" in it) {
          it.initSvg();
        }
      }
      continue;
    }

    //if the json has a corresponding argument
    if (typeof jsonElement !== "object" && BlocklyMiscUtils.inputNameToBlockType(name) === "ValueProvider") {
      //integer block stuff
      handleIntArg(block, inputArg.name, workspace, jsonElement);
    } else if (name === "spells" || name === "conditions" || name === "filters" || name === "cards") {
      //arrays of things stuff
      handleArrayArg(jsonElement, block, workspace, name);
    } else if (name === "trigger" || name === "pact") {
      enchantment(json[name], workspace, block.getFirstStatementConnection().targetBlock());
    } else if (name === "aura") {
      auras(block, json, workspace);
    } else if (name === "cardCostModifier") {
      costModifier(block, json.cardCostModifier, workspace);
    } else {
      //default recursion case
      handleArg(block.getInput(argName).connection, jsonElement, name, workspace, json);
    }
  }
}

export function handleArrayArg(jsonElement, block: Block | BlockSvg, workspace, name) {
  let thingArray = jsonElement;
  let lowestBlock = block.getFirstStatementConnection().targetBlock();
  handleArg(lowestBlock.getInput("i").connection, thingArray[0], name.slice(0, -1), workspace, thingArray);
  for (let i = 1; i < thingArray.length; i++) {
    let thingI;
    switch (name) {
      case "conditions":
        thingI = BlocklyMiscUtils.newBlock(workspace, "Condition_I");
        break;
      case "filters":
      case "cardFilters":
        thingI = BlocklyMiscUtils.newBlock(workspace, "Filter_I");
        break;
      case "cards":
        thingI = BlocklyMiscUtils.newBlock(workspace, "Card_I");
        break;
      default:
        thingI = BlocklyMiscUtils.newBlock(workspace, "Spell_I");
        break;
    }
    handleArg(thingI.getInput("i").connection, thingArray[i], name.slice(0, -1), workspace, thingArray);
    lowestBlock.nextConnection.connect(thingI.previousConnection);
    if ("initSvg" in thingI) {
      thingI.initSvg();
    }
    lowestBlock = thingI;
  }
}

/**
 * Many blocks serve as 'wrappers' for other blocks, ending up being converted
 * to card json as a modification of what they hold inside (think, random from [Target(s)])
 *
 * This method handles all the wrapper blocks that could be encountered
 * when handling an arg, successfully wrapping even in cases where
 * multiple wrappers are needed.
 * @param block The original block in need of wrapping
 * @param json The json that the block corresponds to
 * @param inputName The name of the argument that json is assigned to
 * @param workspace The workspace
 * @param parentJson The level of json above json
 * @param connection The connection that the block was originally supposed to connect to
 * @param bestMatch The block that was decided to be the best match
 * @returns The eventual outermost block
 */
export function wrapperBlocks(block, json, inputName, workspace, parentJson, connection, bestMatch) {
  const wrap = (blockType, inputName = "super") => {
    let newOuterBlock = BlocklyMiscUtils.newBlock(workspace, blockType);
    newOuterBlock.getInput(inputName).connection.connect(outerBlock.outputConnection);
    if ("initSvg" in newOuterBlock) {
      newOuterBlock.initSvg();
    }
    outerBlock = newOuterBlock;
  };
  let outerBlock = block;

  if (
    !!json.targetPlayer &&
    !!bestMatch &&
    (json.targetPlayer !== "SELF" || json.class === "ReturnTargetToHandSpell") &&
    (bestMatch.output === "ValueProviderDesc" || bestMatch.output === "SpellDesc" || bestMatch.output === "Source")
  ) {
    switch (bestMatch.output) {
      case "ValueProviderDesc":
        wrap("ValueProvider_targetPlayer");
        break;
      case "Source":
        wrap("Source_targetPlayer");
        break;
      default:
        wrap("Spell_TargetPlayer");
        break;
    }
    simpleHandleArg(outerBlock, "targetPlayer", json, workspace);
  }

  if (
    inputName === "target" &&
    !!parentJson.target &&
    !!parentJson.filter &&
    !getInputEndsWith(connection.getSourceBlock(), "filter") &&
    !connection.getSourceBlock().getInput("filter")
  ) {
    wrap("EntityReference_FILTER");
    handleArg(outerBlock.getInput("super.filter").connection, parentJson.filter, "filter", workspace, json);
  }

  if (
    !!json.invert &&
    !!bestMatch &&
    !argsList(bestMatch)
      .map((arg) => arg.name.split(".").slice(-1)[0])
      .includes("invert")
  ) {
    if (json.class.endsWith("Filter")) {
      wrap("Filter_NOT");
    } else if (json.class.endsWith("Condition")) {
      wrap("Condition_NOT");
    }
  }

  if (!!json.class && json.class.endsWith("Trigger")) {
    if (!!json.fireCondition) {
      wrap("Trigger_FireCondition");
      simpleHandleArg(outerBlock, "fireCondition", json, workspace);
    }
    if (!!json.queueCondition) {
      wrap("Trigger_QueueCondition");
      simpleHandleArg(outerBlock, "queueCondition", json, workspace);
    }
    if (!!json.race) {
      wrap("Trigger_Race");
      simpleHandleArg(outerBlock, "race", json, workspace);
    }
    if (!!json.requiredAttribute) {
      let match = getMatch(json.requiredAttribute, "attribute", json);
      wrap("Trigger_Attribute");
      simpleHandleArg(outerBlock, "requiredAttribute", json, workspace);
    }
  }

  if (
    inputName.endsWith("targetSelection") &&
    !!parentJson.targetSelectionCondition &&
    !!parentJson.targetSelectionOverride
  ) {
    wrap("TargetSelection_OVERRIDE");
    handleArg(
      outerBlock.getInput("super.targetSelectionCondition").connection,
      parentJson.targetSelectionCondition,
      "targetSelectionCondition",
      workspace,
      parentJson
    );
    handleArg(
      outerBlock.getInput("super.targetSelectionOverride").connection,
      parentJson.targetSelectionOverride,
      "targetSelectionOverride",
      workspace,
      parentJson
    );
  }

  if (inputName.endsWith("targetSelection") && !!parentJson.spell && !!parentJson.spell.filter && json !== "NONE") {
    if (parentJson.spell.filter.class === "RaceFilter") {
      wrap("TargetSelection_RACE");
      handleArg(
        outerBlock.getInput("super.spell.filter.race").connection,
        parentJson.spell.filter.race,
        "race",
        workspace,
        parentJson.spell.filter
      );
    } else {
      wrap("TargetSelection_FILTER");
      handleArg(
        outerBlock.getInput("super.spell.filter").connection,
        parentJson.spell.filter,
        "filter",
        workspace,
        parentJson.spell
      );
    }
  }

  if (inputName === "target" && parentJson.randomTarget === true) {
    //handles the randomTarget arg
    wrap("EntityReference_RANDOM");
  }

  if (!!json.multiplier) {
    wrap("ValueProvider_multiplier");
    if (typeof json.multiplier !== "object") {
      handleIntArg(outerBlock, "multiplier", workspace, json.multiplier);
    } else {
      simpleHandleArg(outerBlock, "multiplier", json, workspace);
    }
  }

  if (!!json.offset) {
    wrap("ValueProvider_offset");
    if (typeof json.offset !== "object") {
      handleIntArg(outerBlock, "offset", workspace, json.offset);
    } else {
      simpleHandleArg(outerBlock, "offset", json, workspace);
    }
  }

  if (!!json.distinct) {
    wrap("Source_distinct");
  }

  return outerBlock;
}

/**
 * In many places specifying everything needed by handleArg is redundent,
 * so this method can handle the simple cases simply
 * @param block The block being connected to
 * @param inputName The name of BOTH the block argument and the json argument
 * @param json The PARENT json in which the real json is found by inputName
 * @param workspace The workspace
 * @returns The block that handleArg returns
 */
export function simpleHandleArg(block, inputName, json, workspace) {
  return handleArg(block.getInput(inputName).connection, json[inputName], inputName, workspace, json);
}

/**
 * Traverses through the json of card to try to find the element
 * being referred to by a block's argument, which could include
 * features like 'super', '.' and ',' as explained in WorkspaceUtils
 *
 * In cases of ',' this returns the first match it encounters
 * @param name The name to search for, possibly containing special elements
 * @param json The json to search in
 * @param parentJson The level above the json to search in (for super purposes)
 * @returns What's in the correct spot, or undefined if it can't find the right spot
 */
export function traverseJsonByArgName(name, json, parentJson) {
  if (!name || !json) {
    return undefined;
  }
  if (name.includes(",")) {
    let names = name.split(",");
    for (let name of names) {
      let elem = traverseJsonByArgName(name, json, parentJson);
      if (elem !== undefined) {
        return elem;
      }
    }
    return undefined;
  } else {
    let i = name.indexOf(".");
    if (i <= 0) {
      return json[name];
    }
    let start = name.substring(0, i);
    let rest = name.substring(i + 1);
    if (start === "super" && !!parentJson) {
      return traverseJsonByArgName(rest, parentJson, null);
    } else {
      return traverseJsonByArgName(rest, json[start], json);
    }
  }
}

/**
 * Specifically handles a spot where an integer value is needed
 *
 * Uses the shadow int block if it's already there,
 * or makes a nonshadow one if it isn't
 * @param block The block that the int block should be connected to
 * @param inputArg The name of the int argument
 * @param workspace The workspace
 * @param int The number that should actually end up in the int block
 */
export function handleIntArg(block: Block | BlockSvg, inputArg, workspace, int) {
  let valueBlock;
  if (
    !!block.getInput(inputArg).connection.targetBlock() &&
    block.getInput(inputArg).connection.targetBlock().type === "ValueProvider_int"
  ) {
    valueBlock = block.getInput(inputArg).connection.targetBlock();
  } else {
    valueBlock = BlocklyMiscUtils.newBlock(workspace, "ValueProvider_int");
    block.getInput(inputArg).connection.connect(valueBlock.outputConnection);
    if ("initSvg" in valueBlock) {
      valueBlock.initSvg();
    }
  }
  valueBlock.setFieldValue(int, "int");
}

/**
 * The old way of doing custom blocks, which was actually defining
 * a new block with the needed input types and field values built into it
 *
 * Now, this is only used to generate some json to print to the console
 * that you can use to quickly make the block yourself,
 * but the actual block(s) that end up on the workspace
 * are from handleNoMatch and use stuff from the Custom tab
 *
 * @param json The json of the card that needs its own block
 * @param inputName The name of the argument json is assigned to
 * @param parentJson The level of json that's above json
 * @returns The block it generated
 * */
export function generateDummyBlock(json, inputName, parentJson) {
  inputName = inputName.split(".").slice(-1)[0];
  let type = BlocklyMiscUtils.inputNameToBlockType(inputName);
  let consoleBlock;
  if (typeof json !== "object") {
    let color = blockTypeColors[type];
    consoleBlock = {
      type: type + "_" + json.toString(),
      data: json.toString(),
      colour: isNumeric(color) ? parseInt(color) : color,
      output: type,
      message0: BlocklyMiscUtils.toTitleCaseCorrected(json.toString()),
    };
  } else {
    let props = relevantProperties(json);
    let className = json.class;
    let messages = [];
    let args = [];
    for (let prop of props) {
      let shouldBeField = !BlocklyMiscUtils.inputNameToBlockType(prop);
      if (!!json.class && json.class.endsWith("Trigger") && (prop === "targetPlayer" || prop === "sourcePlayer")) {
        shouldBeField = true;
      }
      let arg: BlockArgDef = {
        name: prop,
      };
      let newMessage = prop + ": %1";
      if (shouldBeField) {
        if (prop === "operation") {
          if (
            dropdownsList(Blockly.Blocks["ValueProvider_Algebraic"].json)[0]
              .options.map((arr) => arr[1])
              .includes(json[prop])
          ) {
            arg = dropdownsList(Blockly.Blocks["ValueProvider_Algebraic"].json)[0];
          } else {
            arg = dropdownsList(Blockly.Blocks["Condition_Comparison"].json)[0];
          }
        } else {
          arg.type = "field_label_serializable_hidden";
          arg.value = json[prop];
          newMessage += '"' + BlocklyMiscUtils.toTitleCaseCorrected(json[prop].toString()) + '"';
        }
      } else {
        arg.type = "input_value";
        arg.check =
          (prop === "attribute" ? (!!parentJson.value ? "Int" : "Bool") : "") +
          BlocklyMiscUtils.blockTypeToOuput(BlocklyMiscUtils.inputNameToBlockType(prop));
        arg.shadow = {
          type:
            prop === "target"
              ? "EntityReference_IT"
              : BlocklyMiscUtils.inputNameToBlockType(prop) +
                (prop === "attribute" ? (!!parentJson.value ? "_INT_SHADOW" : "_BOOL_SHADOW") : "_SHADOW"),
        };
      }
      messages.push(newMessage);
      args.push(arg);
    }

    let output = BlocklyMiscUtils.blockTypeToOuput(type);
    let color = blockTypeColors[output];
    consoleBlock = {
      type: type + "_" + className.replace(type, ""),
      inputsInline: false,
      output: output,
      colour: isNumeric(color) ? parseInt(color) : color,
      message0: className + "%1",
      args0: [
        {
          type: "field_label_serializable_hidden",
          name: "class",
          value: className,
        },
      ],
    };
    if (type === "Aura") {
      delete consoleBlock.output;
      consoleBlock.previousStatement = ["Auras"];
      consoleBlock.nextStatement = ["Auras"];
    }
    for (let j = 1; j <= messages.length; j++) {
      //block['message' + j.toString()] = messages[j - 1]
      //block['args' + j.toString()] = [args[j - 1]]

      consoleBlock.message0 += " " + messages[j - 1].replace("%1", "%" + (j + 1).toString());
      consoleBlock.args0.push(args[j - 1]);
    }
  }

  console.log("Had to create new block " + consoleBlock.type);
  console.log(JSON.stringify(consoleBlock, null, 2).toString().replace('"colour": "(\\d+)"', '"colour": $1'));

  let blok = JSON.stringify(consoleBlock, null, 2);
  //blok = consoleBlock.type
  if (!customBlocks[blok]) {
    customBlocks[blok] = 0;
  }
  customBlocks[blok]++;

  return consoleBlock;
}

/**
 * Handles the construction of a custom block for a given bit of json
 *
 * No longer actually generates any new blocks, but uses the tools
 * in the Custom tab of the toolbox
 *
 * @param json The json that needs to be turned into a custom block
 * @param inputName The name of the argument json is assigned to
 * @param parentJson The level of json that's above json
 * @param workspace The workspace
 * @returns {Blockly.Block}
 */
export function handleNoMatch(json, inputName, parentJson, workspace) {
  inputName = inputName.split(".").slice(-1)[0];
  let type = BlocklyMiscUtils.inputNameToBlockType(inputName);
  let block = BlocklyMiscUtils.newBlock(workspace, "Custom" + type);
  if (typeof json !== "object") {
    block.setFieldValue(json, "value");
  } else if (!!json.class) {
    block.getInput("class").connection.targetBlock().setFieldValue(json.class, "class");
    let lowestConnection = block.getFirstStatementConnection();

    for (let arg in json) {
      if (
        arg === "class" ||
        (arg === "filter" && !!parentJson.targetSelection && parentJson.targetSelection !== "NONE")
      ) {
        continue;
      }

      let blockType = BlocklyMiscUtils.inputNameToBlockType(arg);

      let argValue = json[arg];
      if (!blockType) {
        if (argValue === true || argValue === false) {
          blockType = "Boolean";
          argValue = argValue.toString().toUpperCase();
        } else if (isArray(argValue)) {
          blockType = BlocklyMiscUtils.inputNameToBlockType(arg.slice(0, -1)) + "s";
        } else {
          blockType = "text";
        }
      }
      let newArgBlock = BlocklyMiscUtils.newBlock(workspace, "CustomArg_" + blockType);
      newArgBlock.previousConnection.connect(lowestConnection);
      lowestConnection = newArgBlock.nextConnection;
      if ("initSvg" in newArgBlock) {
        newArgBlock.initSvg();
      }
      newArgBlock.setFieldValue(arg, "customArg");

      if (!!newArgBlock.getInput("customValue")) {
        if (isNumeric(argValue)) {
          handleIntArg(newArgBlock, "customValue", workspace, argValue);
        } else if (isArray(argValue)) {
          if (arg === "aura") {
            auras(newArgBlock, json, workspace);
          } else {
            handleArrayArg(argValue, newArgBlock, workspace, arg);
          }
        } else {
          handleArg(newArgBlock.getInput("customValue").connection, argValue, arg, workspace, parentJson);
        }
      } else {
        newArgBlock.setFieldValue(argValue, "customValue");
      }
    }
  }
  return block;
}

/**
 * The arguments of a desc that should be used in deciding on a block representation
 * @param json
 * @returns array of the relevant properties (strings)
 */
export function relevantProperties(json) {
  let relevantProperties = [];
  for (let property in json) {
    if (
      (property === "randomTarget" && !!json.target) ||
      property === "class" ||
      property === "fireCondition" ||
      property === "queueCondition" ||
      property === "invert" ||
      property === "offset" ||
      property === "multiplier" ||
      property === "distinct"
    ) {
      continue; //these ones can be handled by other blocks
    }
    if (json[property] !== null && json[property] !== undefined) {
      relevantProperties.push(property);
    }
  }
  return relevantProperties;
}

/**
 * Deals with places in card json that could be equivalent in function to different json,
 * but need special handling to be creatable in the card editor
 * @param json
 * @returns The mutated json
 */
export function mutateJson(json) {
  if (json === null || json === undefined) {
    json = "NONE";
  }
  if (typeof json !== "object") {
    return json;
  }
  let className = json.class;
  let props = relevantProperties(json);

  //the 'has' operation can be implied in many cases
  if (className.endsWith("Filter") || className.endsWith("Condition")) {
    if (
      props.includes("attribute") &&
      props.includes("operation") &&
      !props.includes("value") &&
      json.operation === "HAS"
    ) {
      delete json.operation;
      props = relevantProperties(json);
    }
  }

  if (className === "AndCondition" && (!json.conditions || json.conditions?.length === 0)) {
    delete json.conditions;
    json.class = "AlwaysCondition";
  }

  if (className === "OrCondition" && (!json.conditions || json.conditions?.length === 0)) {
    delete json.conditions;
    json.class = "NeverCondition";
  }

  if (className === "CardFilter") {
    if (props.length === 1) {
      //cardfilters for just a race can be race filters
      if (props[0] === "race") {
        return {
          class: "RaceFilter",
          race: json.race,
          invert: json.invert,
        };
      }
      if (props[0] === "attribute") {
        return {
          class: "AttributeFilter",
          attribute: json.attribute,
          invert: json.invert,
        };
      }
    } else if (
      !(
        props.length === 2 &&
        props.includes("attribute") &&
        (props.includes("value") || props.includes("operation"))
      ) &&
      !(props.length === 3 && props.includes("attribute") && props.includes("operation") && props.includes("value"))
    ) {
      //cardfilters with many different properties need to be split up to a big and filter
      let filters = [];
      if (!!json.race) {
        filters.push({
          class: "RaceFilter",
          race: json.race,
        });
      }
      if (!!json.cardType) {
        filters.push({
          class: "CardFilter",
          cardType: json.cardType,
        });
      }
      if (!!json.attribute) {
        if (!json.value && (!json.operation || json.operation === "HAS") && !!enumBlocksDictionary[json.attribute]) {
          if (enumBlocksDictionary[json.attribute][0].output === "BoolAttribute") {
            filters.push({
              class: "AttributeFilter",
              attribute: json.attribute,
            });
          } else {
            filters.push({
              class: "AttributeFilter",
              attribute: json.attribute,
              operation: "GREATER_OR_EQUAL",
              value: 1,
            });
          }
        } else {
          filters.push({
            class: "AttributeFilter",
            attribute: json.attribute,
            operation: json.operation,
            value: json.value,
          });
        }
      }
      if (json.hasOwnProperty("manaCost")) {
        filters.push({
          class: "CardFilter",
          manaCost: json.manaCost,
        });
      }
      if (!!json.heroClass) {
        filters.push({
          class: "CardFilter",
          heroClass: json.heroClass,
        });
      }
      if (!!json.rarity) {
        filters.push({
          class: "CardFilter",
          rarity: json.rarity,
        });
      }
      return {
        class: "AndFilter",
        filters: filters,
        invert: json.invert,
      };
    }
  }

  //would be redundant to add this functionality separately
  if (className === "MinionCountCondition") {
    json = {
      class: "ComparisonCondition",
      value1: {
        class: "EntityCountValueProvider",
        target:
          json.targetPlayer === "OPPONENT"
            ? "ENEMY_MINIONS"
            : json.targetPlayer === "BOTH"
            ? "ALL_MINIONS"
            : "FRIENDLY_MINIONS",
      },
      operation: json.operation,
      value2: json.value,
    };
  }
  if (className === "CardCountCondition") {
    json = {
      class: "ComparisonCondition",
      value1: {
        class: "CardCountValueProvider",
        targetPlayer: json.targetPlayer,
        cardFilter: !!json.filter ? json.filter : json.cardFilter,
      },
      operation: json.operation,
      value2: json.value,
    };
  }
  /*if (className === 'DeckContainsCondition') {
    if (!!json.card) {
      json = {
        class: 'ComparisonCondition',
        value1: {
          class: 'CardCountValueProvider',
          cardSource: {
            class: 'DeckSource'
          },
          cardFilter: {
            class: 'SpecificCardFilter',
            card: json.card
          }
        },
        operation: "GREATER_OR_EQUAL",
        value2: 1
      }
    } else {
      json = {
        class: 'ComparisonCondition',
        value1: {
          class: 'CardCountValueProvider',
          cardSource: {
            class: 'DeckSource'
          },
          cardFilter: json.cardFilter
        },
        operation: "GREATER_OR_EQUAL",
        value2: 1
      }
    }
  }*/

  //some auras specify extra triggers unnecessarily
  if (className.endsWith("Aura") && !!json.triggers) {
    let triggersDontMatter = true;
    for (let trigger of json.triggers) {
      if (trigger.class !== "WillEndSequenceTrigger") {
        triggersDontMatter = false;
      }
    }
    if (triggersDontMatter) {
      delete json.triggers;
    }
  }

  //in spells, the targetPlayer of 'self' is always redundant
  if (
    (json.targetPlayer === "SELF" && json.class.endsWith("Spell") && json.class !== "ReturnTargetToHandSpell") ||
    //in triggers, it's 'both'
    (json.targetPlayer === "BOTH" && json.class.endsWith("Trigger"))
  ) {
    delete json.targetPlayer;
  }

  //for certain triggers the Source Player and Target Player are just the same
  if ((className === "MinionSummonedTrigger" || className === "MinionPlayedTrigger") && !!json.sourcePlayer) {
    json.targetPlayer = json.sourcePlayer;
    delete json.sourcePlayer;
  }

  if (className === "TemporaryAttackSpell" && !!json.attackBonus) {
    if (!json.value) {
      json.value = 0;
    }
    json.value += json.attackBonus;
    delete json.attackBonus;
  }

  if (json.sourcePlayer === "BOTH") {
    delete json.sourcePlayer;
  }

  if (className === "ReduceValueProvider" && json.attribute) {
    json.value1 = {
      class: "AttributeValueProvider",
      attribute: json.attribute,
    };
    delete json.attribute;
  }

  if (className === "DiscoverSpell" && !json.cards && !json.cardSource) {
    json.cardSource = {
      class: "CatalogueSource",
    };
  }

  if (className === "AlgebraicValueProvider" && json.operation === "NEGATE" && !json.hasOwnProperty("value2")) {
    json.operation = "MULTIPLY";
    json.value2 = -1;
  }

  if (className === "RecruitSpell" && !json.cardLocation) {
    json.cardLocation = "DECK";
  }

  if (className.endsWith("Modifier") && !json.target) {
    json.target = "FRIENDLY_HAND";
  }

  if (className.endsWith("Aura") && !!json.triggers && json.triggers.length === 1) {
    json.trigger = json.triggers[0];
    delete json.triggers;
  }

  //functionality is the same
  if (className === "CloneMinionSpell") {
    json.class = "SummonSpell";
  }
  if (className === "InspireTrigger") {
    json.class = "HeroPowerUsedTrigger";
  }

  return json;
}

/**
 * Returns a list of the all the arguments in a block's json
 * that are input args
 * @param block
 * @returns An array of the input args
 */
export function inputsList(block) {
  let inputsList = [];
  for (let i = 0; i < 10; i++) {
    if (!!block["args" + i.toString()]) {
      for (let j = 0; j < 10; j++) {
        const arg = block["args" + i.toString()][j];
        if (!!arg && arg.type.includes("input")) {
          inputsList.push(arg);
        }
      }
    }
  }
  return inputsList;
}

/**
 * Returns a list of the all the arguments in a block's json
 * that are dropdown args
 * @param block
 * @returns An array of the dropdown args
 */
export function dropdownsList(block: Block | BlockSvg) {
  let inputsList = [];
  for (let i = 0; i < 10; i++) {
    if (!!block["args" + i.toString()]) {
      for (let j = 0; j < 10; j++) {
        const arg = block["args" + i.toString()][j];
        if (!!arg && arg.type.includes("dropdown")) {
          inputsList.push(arg);
        }
      }
    }
  }
  return inputsList;
}

/**
 * Returns a list of the all the arguments in a block's json
 * @param block
 * @returns An array of the args
 */
export function argsList(block: BlockDef) {
  let argsList = [];
  for (let i = 0; i < 10; i++) {
    if (!!block["args" + i.toString()]) {
      for (let j = 0; j < 10; j++) {
        const arg = block["args" + i.toString()][j];
        if (!!arg) {
          argsList.push(arg);
        }
      }
    }
  }
  return argsList;
}
