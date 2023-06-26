import Blockly from "blockly";
import javascript from "blockly/javascript";
import * as WorkspaceUtils from "./workspace-utils";

const ORDER_NONE = 99;

export function generateJavaScript() {
  javascript["TestStarter_RunGym"] = function () {
    return "const context = SpellsourceTesting.runGym()\n";
  };

  javascript["TestStarter_RunGym2"] = function (block) {
    let friendlyClass = javascript.valueToCode(block, "friendlyClass", ORDER_NONE);
    let enemyClass = javascript.valueToCode(block, "enemyClass", ORDER_NONE);
    return "const context = SpellsourceTesting.runGym(" + friendlyClass + ", " + enemyClass + ")\n";
  };

  javascript["TestAssertion"] = function (block) {
    let condition = block.getInput("condition").connection.targetBlock();
    let json = blockToJson(condition);
    return "expect(SpellsourceTesting.condition(`" + JSON.stringify(json, null, 2) + "`, context)).toEqual(true)";
  };

  javascript["TestActionSpellEffect"] = function (block) {
    let spell = block.getInput("spell").connection.targetBlock();
    let json = blockToJson(spell);
    return "SpellsourceTesting.spell(`" + JSON.stringify(json, null, 2) + "`, context)\n";
  };

  javascript["TestActionPlayCard"] = function (block) {
    let card = javascript.valueToCode(block, "card", ORDER_NONE);
    let player = javascript.valueToCode(block, "player", ORDER_NONE);
    let target = javascript.valueToCode(block, "target", ORDER_NONE);
    let ret = "SpellsourceTesting.playCard(context, " + player + ", " + card;
    if (target !== "'NONE'") {
      ret += ", " + target;
    }
    return ret + ")\n";
  };

  javascript["TestActionPlayMinion"] = function (block) {
    let card = javascript.valueToCode(block, "card", ORDER_NONE);
    let player = javascript.valueToCode(block, "player", ORDER_NONE);
    let target = javascript.valueToCode(block, "target", ORDER_NONE);
    let variable = block.getField("variable").getVariable().name;
    let ret = variable + " = SpellsourceTesting.playMinion(context, " + player + ", " + card;
    if (target !== "'NONE'") {
      ret += ", " + target;
    }
    return ret + ")\n";
  };

  javascript["TestActionReceiveCard"] = function (block) {
    let card = javascript.valueToCode(block, "card", ORDER_NONE);
    let player = javascript.valueToCode(block, "player", ORDER_NONE);
    let variable = block.getField("variable").getVariable().name;
    return variable + " = SpellsourceTesting.receiveCard(context, " + player + ", " + card + ")\n";
  };

  for (let blocksKey in Blockly.Blocks) {
    if (Blockly.Blocks[blocksKey]?.json?.output === "ConditionDesc") {
      javascript[blocksKey] = function (block) {
        let xml = Blockly.Xml.blockToDom(block, true);
        let json = WorkspaceUtils.xmlToCardScript(xml);
        return ["SpellsourceTesting.condition(`" + JSON.stringify(json, null, 2) + "`, context)", ORDER_NONE];
      };
    }

    if (Blockly.Blocks[blocksKey]?.json?.output === "ValueProviderDesc") {
      javascript[blocksKey] = function (block) {
        let xml = Blockly.Xml.blockToDom(block, true);
        let json = WorkspaceUtils.xmlToCardScript(xml);
        return ["SpellsourceTesting.value(`" + JSON.stringify(json, null, 2) + "`, context)", ORDER_NONE];
      };
    }

    if (blocksKey.startsWith("Starter_")) {
      javascript[blocksKey] = function (block) {
        let xml = Blockly.Xml.blockToDom(block, true);
        let json = WorkspaceUtils.xmlToCardScript(xml);
        let id = block.getFieldValue("id");
        let cardId = Blockly.Blocks["WorkspaceCard_" + id]?.data;

        json.id = cardId;

        javascript["cardsDB"][cardId] = json;

        return "";
      };
    }

    if (!javascript[blocksKey]) {
      if (!!Blockly.Blocks[blocksKey].json?.data && !!Blockly.Blocks[blocksKey].json?.output) {
        javascript[blocksKey] = function (block) {
          return ["'" + Blockly.Blocks[blocksKey].json.data + "'", ORDER_NONE];
        };
      } else {
        javascript[blocksKey] = function (block) {
          return "";
        };
      }
    }
  }

  const init = javascript.init;
  javascript.init = function (workspace) {
    javascript["cardsDB"] = {};
    return init.call(this, workspace);
  };

  const finish = javascript.finish;
  javascript.finish = function (code) {
    for (let cardId in javascript["cardsDB"]) {
      let json = javascript["cardsDB"][cardId];
      let addCode = "SpellsourceTesting.addCard(`" + JSON.stringify(json, null, 2) + "`)";
      let removeCode = "SpellsourceTesting.removeCard('" + cardId + "')";
      code = addCode + "\n\n" + code + "\n\n" + removeCode;
    }
    delete javascript["cardsDB"];
    return finish.call(this, code);
  };
}

function fixJsonVariables(json) {
  if (json["VAR"]) {
    let VAR = json["VAR"];
    return "` + " + VAR + " + `";
  }
  for (const jsonKey in json) {
    if (json.hasOwnProperty(jsonKey) && json.propertyIsEnumerable(jsonKey) && typeof json[jsonKey] !== "string") {
      json[jsonKey] = this.fixJsonVariables(json[jsonKey]);
    }
  }
  return json;
}

function blockToJson(block: Blockly.Block) {
  let xml = Blockly.Xml.blockToDom(block, true);
  let json = WorkspaceUtils.xmlToCardScript(xml);
  return this.fixJsonVariables(json);
}
