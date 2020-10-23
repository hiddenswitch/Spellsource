import fs from 'fs'
import path from 'path'
import java from 'java'
import Blockly from 'blockly'
import WorkspaceUtils from './workspace-utils'


java.classpath.push(`${__dirname}/../../../../www/build/libs/www-0.8.89-all.jar`)

export default class SpellsourceTesting {

  static runGym(heroclass1 = null, heroclass2 = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.runGymSync(heroclass1, heroclass2)
  }

  static spell(json, context, source = null, target = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    TestMain.spellEffectSync(json, context, source, target)
  }

  static condition(json, context, source = null, target = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.evalConditionSync(json, context, source, target)
  }

  static value(json, context, source = null, target = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.evalValue(json, context, source, target)
  }

  static target(entityReference, context, player=null, source = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.playerSync(player, context)
    }
    return TestMain.singleEntitySync(entityReference.toString(), context, player, source)
  }

  static playCard(context, player, card, target = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.playerSync(player, context)
    }
    if (typeof card === "number") {
      card = TestMain.singleEntitySync(card.toString(), context, player, player)
      if (!target) {
        TestMain.cardRefSync(context, player, card)
      } else {
        target = TestMain.singleEntitySync(target.toString(), context, player, player)
        TestMain.cardRefTargetSync(context, player, card, target)
      }
    } else {
      if (!target) {
        TestMain.cardSync(context, player, card)
      } else {
        target = TestMain.singleEntitySync(target.toString(), context, player, player)
        TestMain.cardTargetSync(context, player, card, target)
      }
    }

  }

  static playMinion(context, player, card, target = null) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.playerSync(player, context)
    }
    if (typeof card === "number") {
      card = TestMain.singleEntitySync(card.toString(), context, player, player)
      if (!target) {
        return TestMain.minionCardRefSync(context, player, card)
      } else {
        target = TestMain.singleEntitySync(target.toString(), context, player, player)
        return TestMain.minionCardRefTargetSync(context, player, card, target)
      }
    } else {
      if (!target) {
        return TestMain.minionCardSync(context, player, card)
      } else {
        target = TestMain.singleEntitySync(target.toString(), context, player, player)
        return TestMain.minionCardTargetSync(context, player, card, target)
      }
    }

  }

  static receiveCard(context, player, cardid) {
    const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.playerSync(player, context)
    }
    return TestMain.receiveSync(context, player, cardid)
  }
}