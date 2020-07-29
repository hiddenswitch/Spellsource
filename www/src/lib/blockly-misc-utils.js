import Blockly from "blockly";
import JsonConversionUtils from "./json-conversion-utils";

export default class BlocklyMiscUtils {

  static toHappyFormatting(string) {
    return string.split('_')
      .map(w => w[0].toUpperCase() + w.substr(1).toLowerCase())
      .join(' ')
  }

  static addBlock(block) {
    Blockly.Blocks[block.type] = {
      init: function () {
        this.jsonInit(block)
        if (!!block.data) {
          this.data = block.data
        }
        if (!!block.hat) {
          this.hat = block.hat
        }
        if (block.type.endsWith('SHADOW')) {
          this.setMovable(false)
        }
      }
    }
    Blockly.Blocks[block.type].json = block
    JsonConversionUtils.addBlockToMap(block)
  }

  //initializes the json specified shadow blocks of a block on the workspace
  static manuallyAddShadowBlocks(thisBlock, block) {
    for (let i = 0; i < 10; i++) {
      if (!!block['args' + i.toString()]) {
        for (let j = 0; j < 10; j++) {
          const arg = block['args' + i.toString()][j]
          if (!!arg) {
            const shadow = arg.shadow
            if (!!shadow) {
              let shadowBlock = JsonConversionUtils.newBlock(thisBlock.workspace, shadow.type)
              if (shadow.notActuallyShadow) {
                shadowBlock.setMovable(false)
              } else {
                shadowBlock.setShadow(true)
              }
              if (!!shadow.fields) {
                for (let field of shadow.fields) {
                  if (field.valueI !== null) {
                    shadowBlock.setFieldValue(field.valueI, field.name)
                  }
                  if (field.valueS !== null) {
                    shadowBlock.setFieldValue(field.valueS, field.name)
                  }
                  if (field.valueB !== null) {
                    shadowBlock.setFieldValue(field.valueB, field.name)
                  }
                }
              }
              const connection = arg.type.endsWith('statement') ?
                shadowBlock.previousConnection : shadowBlock.outputConnection
              thisBlock.getInput(arg.name).connection.connect(connection)
              shadowBlock.initSvg()
              this.manuallyAddShadowBlocks(shadowBlock, Blockly.Blocks[shadow.type].json)
            }
          }
        }
      }
    }


    if (block.type.startsWith('Starter')) {
      let shadowBlock = thisBlock.workspace.newBlock('Property_SHADOW')
      shadowBlock.setShadow(true)
      thisBlock.nextConnection.connect(shadowBlock.previousConnection)
      shadowBlock.initSvg()
    }
  }



  static inputNameToBlockType(inputName) {
    if (inputName.includes('.')) {
      inputName = inputName.split('.').slice(-1)[0]
    }
    switch (inputName) {
      case 'card':
        return 'Card'
      case 'queueCondition':
      case 'fireCondition':
      case 'condition':
        return 'Condition'
      case 'spell':
      case 'spell1':
      case 'spell2':
        return 'Spell'
      case 'filter':
      case 'cardFilter':
        return 'Filter'
      case 'secondaryTarget':
      case 'target':
        return 'EntityReference'
      case 'race':
        return 'Race'
      case 'sourcePlayer':
      case 'targetPlayer':
        return 'TargetPlayer'
      case 'heroClass':
        return 'HeroClass'
      case 'rarity':
        return 'Rarity'
      case 'attribute':
        return 'Attribute'
      case 'targetSelection':
        return 'TargetSelection'
      case 'eventTrigger':
      case 'revertTrigger':
      case 'secondaryTrigger':
        return 'Trigger'
      case 'value':
      case 'howMany':
      case 'ifTrue':
      case 'ifFalse':
      case 'value1':
      case 'value2':
      case 'secondaryValue':
      case 'multiplier':
      case 'offset':
      case 'attackBonus':
      case 'hpBonus':
      case 'armorBonus':
        return 'ValueProvider'
      case 'aura':
        return 'Aura'
      default:
        return null
    }
  }

  static blockTypeToOuput(type) {
    switch (type) {
      case 'Spell':
      case 'ValueProvider':
      case 'Condition':
      case 'Filter':
        return type + 'Desc'
      default:
        return type
    }
  }

}