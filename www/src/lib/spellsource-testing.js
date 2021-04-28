Java.addToClasspath(`${__dirname}/../../build/libs/www-0.9.0-all.jar`)

export default class SpellsourceTesting {

  static runGym(heroclass1 = null, heroclass2 = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.runGym(heroclass1, heroclass2)
  }

  static addCard(json) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.addCard(json)
  }

  static spell(json, context, source = null, target = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    TestMain.spellEffect(json, context, source, target)
  }

  static condition(json, context, source = null, target = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.evalCondition(json, context, source, target)
  }

  static value(json, context, source = null, target = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    return TestMain.evalValue(json, context, source, target)
  }

  static target(entityReference, context, player=null, source = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.player(player, context)
    }
    return TestMain.singleEntity(entityReference.toString(), context, player, source)
  }

  static playCard(context, player, card, target = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.player(player, context)
    }
    if (typeof card === "number") {
      card = TestMain.singleEntity(card.toString(), context, player, player)
      if (!target) {
        TestMain.cardRef(context, player, card)
      } else {
        target = TestMain.singleEntity(target.toString(), context, player, player)
        TestMain.cardRefTarget(context, player, card, target)
      }
    } else {
      if (!target) {
        TestMain.card(context, player, card)
      } else {
        target = TestMain.singleEntity(target.toString(), context, player, player)
        TestMain.cardTarget(context, player, card, target)
      }
    }

  }

  static playMinion(context, player, card, target = null) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.player(player, context)
    }
    if (typeof card === "number") {
      card = TestMain.singleEntity(card.toString(), context, player, player)
      if (!target) {
        return TestMain.minionCardRef(context, player, card)
      } else {
        target = TestMain.singleEntity(target.toString(), context, player, player)
        return TestMain.minionCardRefTarget(context, player, card, target)
      }
    } else {
      if (!target) {
        return TestMain.minionCard(context, player, card)
      } else {
        target = TestMain.singleEntity(target.toString(), context, player, player)
        return TestMain.minionCardTarget(context, player, card, target)
      }
    }

  }

  static receiveCard(context, player, cardid) {
    const TestMain = Java.type('com.hiddenswitch.spellsource.gameplaytest.TestMain')
    if (player) {
      player = TestMain.player(player, context)
    }
    return TestMain.receive(context, player, cardid)
  }
}