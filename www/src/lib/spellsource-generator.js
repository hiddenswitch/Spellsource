import Blockly from 'blockly'
import WorkspaceUtils from "./workspace-utils";


export default class SpellsourceGenerator {

  static generateJavaScript() {
    Blockly.JavaScript['TestStarter_RunGym'] = function (block) {
      return "const context = SpellsourceTesting.runGym()\n"
    }

    Blockly.JavaScript['TestStarter_RunGym2'] = function (block) {
      let friendlyClass = Blockly.JavaScript.valueToCode(block, 'friendlyClass', Blockly.JavaScript.ORDER_NONE)
      let enemyClass = Blockly.JavaScript.valueToCode(block, 'enemyClass', Blockly.JavaScript.ORDER_NONE)
      return "const context = SpellsourceTesting.runGym(" + friendlyClass + ", " + enemyClass + ")\n"
    }

    Blockly.JavaScript['TestSpellEffect'] = function (block) {
      let spell = block.getInput('spell').connection.targetBlock()
      let json = SpellsourceGenerator.blockToJson(spell)
      return "SpellsourceTesting.spell(`" + JSON.stringify(json, null, 2) + "`, context)\n"
    }

    Blockly.JavaScript['TestPlayCard'] = function (block) {
      let card = Blockly.JavaScript.valueToCode(block, 'card', Blockly.JavaScript.ORDER_NONE)
      let player = Blockly.JavaScript.valueToCode(block, 'player', Blockly.JavaScript.ORDER_NONE)
      let target = Blockly.JavaScript.valueToCode(block, 'target', Blockly.JavaScript.ORDER_NONE)
      let ret = "SpellsourceTesting.playCard(context, " +  player + ", " + card
      if (target !== "'NONE'") {
        ret += ", " + target
      }
      return ret + ")\n"
    }

    Blockly.JavaScript['TestPlayMinion'] = function (block) {
      let card = Blockly.JavaScript.valueToCode(block, 'card', Blockly.JavaScript.ORDER_NONE)
      let player = Blockly.JavaScript.valueToCode(block, 'player', Blockly.JavaScript.ORDER_NONE)
      let target = Blockly.JavaScript.valueToCode(block, 'target', Blockly.JavaScript.ORDER_NONE)
      let variable = block.getField('variable').getVariable().name
      let ret = variable + " = SpellsourceTesting.playMinion(context, " +  player + ", " + card
      if (target !== "'NONE'") {
        ret += ", " + target
      }
      return ret + ")\n"
    }
    Blockly.JavaScript['TestReceiveCard'] = function (block) {
      let card = Blockly.JavaScript.valueToCode(block, 'card', Blockly.JavaScript.ORDER_NONE)
      let player = Blockly.JavaScript.valueToCode(block, 'player', Blockly.JavaScript.ORDER_NONE)
      let variable = block.getField('variable').getVariable().name
      return variable + " = SpellsourceTesting.receiveCard(context, " +  player + ", " + card + ")\n"
    }

    for (let blocksKey in Blockly.Blocks) {
      if (Blockly.Blocks[blocksKey]?.json?.output === 'ConditionDesc') {
        Blockly.JavaScript[blocksKey] = function (block) {
          let xml = Blockly.Xml.blockToDom(block, true)
          let json = WorkspaceUtils.xmlToCardScript(xml)
          return ["SpellsourceTesting.condition(`" + JSON.stringify(json, null, 2) + "`, context)",
            Blockly.JavaScript.ORDER_NONE]
        }
      }

      if (Blockly.Blocks[blocksKey]?.json?.output === 'ValueProviderDesc') {
        Blockly.JavaScript[blocksKey] = function (block) {
          let xml = Blockly.Xml.blockToDom(block, true)
          let json = WorkspaceUtils.xmlToCardScript(xml)
          return ["SpellsourceTesting.value(`" + JSON.stringify(json, null, 2) + "`, context)",
            Blockly.JavaScript.ORDER_NONE]
        }
      }

      if (!Blockly.JavaScript[blocksKey]) {
        if (!!Blockly.Blocks[blocksKey].json?.data) {
          Blockly.JavaScript[blocksKey] = function (block) {
            return ["'" + Blockly.Blocks[blocksKey].json.data + "'", Blockly.JavaScript.ORDER_NONE]
          }
        } else {
          Blockly.JavaScript[blocksKey] = function (block) {
            return ''
          }
        }
      }
    }
  }

  static fixJsonVariables(json) {
    if (json["VAR"]) {
      let VAR = json["VAR"]
      return "` + " + VAR + " + `"
    }
    for (const jsonKey in json) {
      if (json.hasOwnProperty(jsonKey) && json.propertyIsEnumerable(jsonKey)
      && typeof json[jsonKey] !== 'string') {
        json[jsonKey] = this.fixJsonVariables(json[jsonKey])
      }
    }
    return json
  }

  static blockToJson(block) {
    let xml = Blockly.Xml.blockToDom(block, true)
    let json = WorkspaceUtils.xmlToCardScript(xml)
    return this.fixJsonVariables(json)
  }
}