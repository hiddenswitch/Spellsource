import Blockly from 'blockly'
import * as WorkspaceUtils from "./workspace-utils";

export function generateJavaScript() {
  Blockly.JavaScript['TestStarter_RunGym'] = function () {
    return "const context = SpellsourceTesting.runGym()\n"
  }

  Blockly.JavaScript['TestStarter_RunGym2'] = function (block) {
    let friendlyClass = Blockly.JavaScript.valueToCode(block, 'friendlyClass', Blockly.JavaScript.ORDER_NONE)
    let enemyClass = Blockly.JavaScript.valueToCode(block, 'enemyClass', Blockly.JavaScript.ORDER_NONE)
    return "const context = SpellsourceTesting.runGym(" + friendlyClass + ", " + enemyClass + ")\n"
  }

  Blockly.JavaScript['TestAssertion'] = function (block) {
    let condition = block.getInput('condition').connection.targetBlock()
    let json = blockToJson(condition)
    return "expect(SpellsourceTesting.condition(`" + JSON.stringify(json, null, 2) + "`, context)).toEqual(true)"
  }

  Blockly.JavaScript['TestActionSpellEffect'] = function (block) {
    let spell = block.getInput('spell').connection.targetBlock()
    let json = blockToJson(spell)
    return "SpellsourceTesting.spell(`" + JSON.stringify(json, null, 2) + "`, context)\n"
  }

  Blockly.JavaScript['TestActionPlayCard'] = function (block) {
    let card = Blockly.JavaScript.valueToCode(block, 'card', Blockly.JavaScript.ORDER_NONE)
    let player = Blockly.JavaScript.valueToCode(block, 'player', Blockly.JavaScript.ORDER_NONE)
    let target = Blockly.JavaScript.valueToCode(block, 'target', Blockly.JavaScript.ORDER_NONE)
    let ret = "SpellsourceTesting.playCard(context, " + player + ", " + card
    if (target !== "'NONE'") {
      ret += ", " + target
    }
    return ret + ")\n"
  }

  Blockly.JavaScript['TestActionPlayMinion'] = function (block) {
    let card = Blockly.JavaScript.valueToCode(block, 'card', Blockly.JavaScript.ORDER_NONE)
    let player = Blockly.JavaScript.valueToCode(block, 'player', Blockly.JavaScript.ORDER_NONE)
    let target = Blockly.JavaScript.valueToCode(block, 'target', Blockly.JavaScript.ORDER_NONE)
    let variable = block.getField('variable').getVariable().name
    let ret = variable + " = SpellsourceTesting.playMinion(context, " + player + ", " + card
    if (target !== "'NONE'") {
      ret += ", " + target
    }
    return ret + ")\n"
  }

  Blockly.JavaScript['TestActionReceiveCard'] = function (block) {
    let card = Blockly.JavaScript.valueToCode(block, 'card', Blockly.JavaScript.ORDER_NONE)
    let player = Blockly.JavaScript.valueToCode(block, 'player', Blockly.JavaScript.ORDER_NONE)
    let variable = block.getField('variable').getVariable().name
    return variable + " = SpellsourceTesting.receiveCard(context, " + player + ", " + card + ")\n"
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

    if (blocksKey.startsWith('Starter_')) {
      Blockly.JavaScript[blocksKey] = function (block) {
        let xml = Blockly.Xml.blockToDom(block, true)
        let json = WorkspaceUtils.xmlToCardScript(xml)
        let id = block.id
        let cardId = Blockly.Blocks['WorkspaceCard_' + id]?.data

        json.id = cardId

        Blockly.JavaScript.cardsDB[cardId] = json

        return ""
      }
    }

    if (!Blockly.JavaScript[blocksKey]) {
      if (!!Blockly.Blocks[blocksKey].json?.data && !!Blockly.Blocks[blocksKey].json?.output) {
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

  const init = Blockly.JavaScript.init
  Blockly.JavaScript.init = function (workspace) {
    Blockly.JavaScript.cardsDB = {}
    return init.call(this, workspace)
  }

  const finish = Blockly.JavaScript.finish
  Blockly.JavaScript.finish = function (code) {
    for (let cardId in Blockly.JavaScript.cardsDB) {
      let json = Blockly.JavaScript.cardsDB[cardId]
      let addCode = "SpellsourceTesting.addCard(`" +
        JSON.stringify(json, null, 2) + "`)"
      let removeCode = "SpellsourceTesting.removeCard('" + cardId + "')"
      code = addCode + '\n\n' + code + '\n\n' + removeCode
    }
    delete Blockly.JavaScript.cardsDB
    return finish.call(this, code)
  }
}

function fixJsonVariables(json) {
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

function blockToJson(block: Blockly.Block) {
  let xml = Blockly.Xml.blockToDom(block, true)
  let json = WorkspaceUtils.xmlToCardScript(xml)
  return this.fixJsonVariables(json)
}
