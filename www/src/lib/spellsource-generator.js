import Blockly from 'blockly'


export default class SpellsourceGenerator {

  static generateJavaScript() {
    Blockly.JavaScript['TestStarter_RunGym'] = function (block) {
      return "const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')\n" +
        "let context = TestMain.runGymSync()\n"
    }

    Blockly.JavaScript['TestStarter_RunGym2'] = function (block) {
      let friendlyClass = Blockly.JavaScript.valueToCode(block, 'friendlyClass', Blockly.JavaScript.ORDER_NONE)
      let enemyClass = Blockly.JavaScript.valueToCode(block, 'enemyClass', Blockly.JavaScript.ORDER_NONE)
      return "const TestMain = java.import('com.hiddenswitch.spellsource.gameplaytest.TestMain')\n" +
        "let context = TestMain.runGymSync(" + friendlyClass + ", " + enemyClass + ")\n" +
        "window.alert(context.getActivePlayerSync().getHeroSync().getHpSync())\n"
    }

    for (let blocksKey in Blockly.Blocks) {
      if (!!Blockly.Blocks[blocksKey].json?.data) {
        Blockly.JavaScript[blocksKey] = function (block) {
          return [Blockly.Blocks[blocksKey].json.data, Blockly.JavaScript.ORDER_NONE]
        }
      }
    }
  }

}