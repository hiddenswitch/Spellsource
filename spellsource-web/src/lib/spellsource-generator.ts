import Blockly from "blockly";
import { javascriptGenerator } from "blockly/javascript";
import * as WorkspaceUtils from "./workspace-utils";

const ORDER_NONE = 99;

export function generateJavaScript() {
  javascriptGenerator.forBlock["TestStarter_RunGym"] = function () {
    return "const context = SpellsourceTesting.runGym()\n";
  };

  javascriptGenerator.forBlock["TestStarter_RunGym2"] = function (block) {
    let friendlyClass = javascriptGenerator.valueToCode(block, "friendlyClass", ORDER_NONE);
    let enemyClass = javascriptGenerator.valueToCode(block, "enemyClass", ORDER_NONE);
    return "const context = SpellsourceTesting.runGym(" + friendlyClass + ", " + enemyClass + ")\n";
  };

  javascriptGenerator.forBlock["TestAssertion"] = function (block) {
    let condition = block.getInput("condition").connection.targetBlock();
    let json = blockToJson(condition);
    return "expect(SpellsourceTesting.condition(`" + JSON.stringify(json, null, 2) + "`, context)).toEqual(true)";
  };

  javascriptGenerator.forBlock["TestActionSpellEffect"] = function (block) {
    let spell = block.getInput("spell").connection.targetBlock();
    let json = blockToJson(spell);
    return "SpellsourceTesting.spell(`" + JSON.stringify(json, null, 2) + "`, context)\n";
  };

  javascriptGenerator.forBlock["TestActionPlayCard"] = function (block) {
    let card = javascriptGenerator.valueToCode(block, "card", ORDER_NONE);
    let player = javascriptGenerator.valueToCode(block, "player", ORDER_NONE);
    let target = javascriptGenerator.valueToCode(block, "target", ORDER_NONE);
    let ret = "SpellsourceTesting.playCard(context, " + player + ", " + card;
    if (target !== "'NONE'") {
      ret += ", " + target;
    }
    return ret + ")\n";
  };

  javascriptGenerator.forBlock["TestActionPlayMinion"] = function (block) {
    let card = javascriptGenerator.valueToCode(block, "card", ORDER_NONE);
    let player = javascriptGenerator.valueToCode(block, "player", ORDER_NONE);
    let target = javascriptGenerator.valueToCode(block, "target", ORDER_NONE);
    let variable = block.getField("variable").getVariable().name;
    let ret = variable + " = SpellsourceTesting.playMinion(context, " + player + ", " + card;
    if (target !== "'NONE'") {
      ret += ", " + target;
    }
    return ret + ")\n";
  };

  javascriptGenerator.forBlock["TestActionReceiveCard"] = function (block) {
    let card = javascriptGenerator.valueToCode(block, "card", ORDER_NONE);
    let player = javascriptGenerator.valueToCode(block, "player", ORDER_NONE);
    let variable = block.getField("variable").getVariable().name;
    return variable + " = SpellsourceTesting.receiveCard(context, " + player + ", " + card + ")\n";
  };

  for (let blocksKey in Blockly.Blocks) {
    if (Blockly.Blocks[blocksKey]?.json?.output === "ConditionDesc") {
      javascriptGenerator.forBlock[blocksKey] = function (block) {
        let json = WorkspaceUtils.blockToCardScript(block);
        return ["SpellsourceTesting.condition(`" + JSON.stringify(json, null, 2) + "`, context)", ORDER_NONE];
      };
    }

    if (Blockly.Blocks[blocksKey]?.json?.output === "ValueProviderDesc") {
      javascriptGenerator.forBlock[blocksKey] = function (block) {
        let json = WorkspaceUtils.blockToCardScript(block);
        return ["SpellsourceTesting.value(`" + JSON.stringify(json, null, 2) + "`, context)", ORDER_NONE];
      };
    }

    if (blocksKey.startsWith("Starter_")) {
      javascriptGenerator.forBlock[blocksKey] = function (block) {
        let json = WorkspaceUtils.blockToCardScript(block);
        let id = block.getFieldValue("id");
        let cardId = Blockly.Blocks["WorkspaceCard_" + id]?.data;

        json.id = cardId;

        javascriptGenerator["cardsDB"][cardId] = json;

        return "";
      };
    }

    if (!javascriptGenerator.forBlock[blocksKey]) {
      if (Blockly.Blocks[blocksKey].json?.data && Blockly.Blocks[blocksKey].json?.output) {
        javascriptGenerator.forBlock[blocksKey] = function (block) {
          return ["'" + Blockly.Blocks[blocksKey].json.data + "'", ORDER_NONE];
        };
      } else {
        javascriptGenerator.forBlock[blocksKey] = function (block) {
          return "";
        };
      }
    }
  }

  const init = javascriptGenerator.init;
  javascriptGenerator.init = function (workspace) {
    javascriptGenerator["cardsDB"] = {};
    return init.call(this, workspace);
  };

  const finish = javascriptGenerator.finish;
  javascriptGenerator.finish = function (code) {
    for (let cardId in javascriptGenerator["cardsDB"]) {
      let json = javascriptGenerator["cardsDB"][cardId];
      let addCode = "SpellsourceTesting.addCard(`" + JSON.stringify(json, null, 2) + "`)";
      let removeCode = "SpellsourceTesting.removeCard('" + cardId + "')";
      code = addCode + "\n\n" + code + "\n\n" + removeCode;
    }
    delete javascriptGenerator["cardsDB"];
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
  return this.fixJsonVariables(WorkspaceUtils.blockToCardScript(block));
}
