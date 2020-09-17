import Blockly, {FieldLabel, Workspace} from 'blockly'
import JsonConversionUtils from './json-conversion-utils'
import {has} from 'lodash'
import recursiveOmitBy from 'recursive-omit-by'
import {FieldLabelSerializableHidden} from '../components/field-label-serializable-hidden'
import {FieldLabelPlural} from "../components/field-label-plural";
import BlocklyModification from "./blockly-modification";

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
        if (!!block.comment && this.isInFlyout
        && !Blockly.getMainWorkspace().hideSpellsourceComments) {
          this.setCommentText(block.comment)
        }
      },
      json: block
    }
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
            if (!!shadow && !thisBlock.getInput(arg.name).connection.targetBlock()) {
              let shadowBlock = JsonConversionUtils.newBlock(thisBlock.workspace, shadow.type)
              if (shadow.notActuallyShadow && !thisBlock.isShadow()) {
                if (shadow.type.endsWith('SHADOW')) {
                  shadowBlock.setMovable(false)
                }
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
              if (!!shadowBlock.initSvg) {
                shadowBlock.initSvg()
              }
              BlocklyMiscUtils.manuallyAddShadowBlocks(shadowBlock, Blockly.Blocks[shadow.type].json)
            }
          }
        }
      }
    }

    if (block.type.startsWith('Starter')) {
      let shadowBlock = thisBlock.workspace.newBlock('Property_SHADOW')
      shadowBlock.setShadow(true)
      thisBlock.nextConnection.connect(shadowBlock.previousConnection)
      if (!!shadowBlock.initSvg) {
        shadowBlock.initSvg()
      }
    }
  }

  static inputNameToBlockType(inputName) {
    if (inputName.includes('.')) {
      inputName = inputName.split('.').slice(-1)[0]
    }
    switch (inputName) {
      case 'heroPower':
      case 'card':
      case 'hero':
        return 'Card'
      case 'cards':
        return 'Cards'
      case 'queueCondition':
      case 'fireCondition':
      case 'condition':
      case 'targetSelectionCondition':
      case 'andCondition':
        return 'Condition'
      case 'spell':
      case 'spell1':
      case 'spell2':
      case 'deathrattle':
      case 'applyEffect':
      case 'removeEffect':
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
      case 'requiredAttribute':
        return 'Attribute'
      case 'targetSelection':
      case 'targetSelectionOverride':
        return 'TargetSelection'
      case 'eventTrigger':
      case 'revertTrigger':
      case 'secondaryTrigger':
      case 'secret':
      case 'quest':
      case 'expirationTrigger':
      case 'secondRevertTrigger':
      case 'toggleOn':
      case 'toggleOff':
      case "trigger":
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
      case 'manaCost':
      case 'mana':
      case 'manaCostModifier':
      case 'minValue':
      case 'min':
      case 'max':
        return 'ValueProvider'
      case 'aura':
        return 'Aura'
      case 'cardSource':
        return 'Source'
      case 'cardCostModifier':
        return 'CostModifier'
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
      case 'CostModifier':
        return type + 'Desc'
      default:
        return type
    }
  }

  //make the message for a generated block for a catalogue/created card
  static cardMessage(card) {
    let ret = ''
    if (card.baseManaCost !== null && card.baseManaCost !== undefined) {
      ret = '(' + card.baseManaCost + ') '
    }
    if (card.type === 'MINION') {
      ret += card.baseAttack + '/' + card.baseHp
    } else {
      ret += BlocklyMiscUtils.toHappyFormatting(card.type)
    }
    ret += ' "' + card.name + '"'
    return ret
  }

  static initBlocks(data) {
    try {
      Blockly.fieldRegistry.register('field_label_serializable_hidden', FieldLabelSerializableHidden)
      Blockly.fieldRegistry.register('field_label_plural', FieldLabelPlural)
      BlocklyModification.modifyAll()
    } catch (e) {
      // already registered
    }


    // All of our spells, triggers, entity reference enum values, etc.
    data.allBlock.edges.forEach(edge => {
      if (has(Blockly.Blocks, edge.node.type)) {
        return
      }

      const block = recursiveOmitBy(edge.node, ({node}) => node === null)

      // Patch back in values from union type
      if (!!block.args) {
        block.args.forEach(args => {
          args.args.forEach(arg => {
            if (!!arg.valueI) {
              arg.value = arg.valueI
              delete arg.valueI
            }
            if (!!arg.valueS) {
              arg.value = arg.valueS
              delete arg.valueS
            }
            if (arg.hasOwnProperty('valueB')) {
              //arg.value = arg.valueB
              //gotta do this because it seems like the block -> xml conversion hates booleans
              if (arg.valueB === true) {
                arg.value = 'TRUE'
              } else if (arg.valueB === false) {
                arg.value = 'FALSE'
              }
              delete arg.valueB
            }

            if (!!data.allIcon && arg.type === 'field_image' && !!arg.src && !arg.src.includes('.')) {
              for (let edge of data.allIcon.edges) {
                let node = edge.node
                if (node.name === arg.src) {
                  arg.src = node.publicURL
                }
              }
            }
          })

          block['args' + args.i.toString()] = args.args
        })
        delete block.args
      }

      if (!!block.messages) {
        block.messages.forEach((message, i) => {
          block['message' + i.toString()] = message
        })
        delete block.messages
      }

      if (!!block.output && !JsonConversionUtils.blockTypeColors[block.output]) {
        JsonConversionUtils.blockTypeColors[block.output] = block.colour
      }

      BlocklyMiscUtils.addBlock(block)
    })
  }

  static initHeroClassColors(data) {
    if (!Blockly.textColor) {
      Blockly.textColor = {}
    }
    Blockly.heroClassColors = {
      ANY: '#A6A6A6'
    }

    /**
     * first pass through the card catalogue to figure out all the collectible
     * hero classes and their colors
     */
    data.allCard.edges.forEach(edge => {
      let card = edge.node
      let type = 'HeroClass_' + card.heroClass
      if (has(Blockly.Blocks, type)) {
        return
      }
      if (card.type === 'CLASS' && card.collectible) {
        let color = Blockly.utils.colour.rgbToHex(
          card.art.primary.r * 255,
          card.art.primary.g * 255,
          card.art.primary.b * 255
        )


        Blockly.heroClassColors[card.heroClass] = color
        let block = {
          'type': type,
          'message0': card.name,
          'output': 'HeroClass',
          'colour': color,
          'data': card.heroClass
        }
        BlocklyMiscUtils.addBlock(block)
        if (!!card.art.body?.vertex) {
          Blockly.textColor[color] = Blockly.utils.colour.rgbToHex(
            card.art.body.vertex.r * 255,
            card.art.body.vertex.g * 255,
            card.art.body.vertex.b * 255
          )
        }
      }
    })
  }

  static initCardBlocks(data) {
    for (let edge of data.allJSON.edges) {
      let node = edge.node
      let json = node.internal.content
      let card = JSON.parse(json)
      if (!card.type || card.type === 'FORMAT' || !card.fileFormatVersion) {
        continue
      }
      let type = 'CatalogueCard_' + node.name
      if (has(Blockly.Blocks, type)) {
        return
      }
      if (Blockly.heroClassColors.hasOwnProperty(card.heroClass)) { //this check if it's *really* collectible
        let color = Blockly.heroClassColors[card.heroClass]
        let block = {
          'type': type,
          'args0': [],
          'message0': BlocklyMiscUtils.cardMessage(card),
          'output': 'Card',
          'colour': color,
          'data': node.name,
          'comment': this.cardDescription(card),
          'json': json
        }
        BlocklyMiscUtils.addBlock(block)
      }
    }
  }

  static cardDescription(card) {
    if (!card.description) {
      return null
    }
    const newLine = 25
    let words = card.description.split(' ')
    if (words.length === 0) {
      return ''
    }
    let desc = '"' + words[0]
    if (!!card.race) {
      desc = this.toHappyFormatting(card.race) + ' ' + desc
    }
    let counter = desc.length
    for (let word of words.slice(1)) {
      if (counter + word.length > newLine) {
        desc += '\n'
        counter = 0
      } else {
        desc += ' '
      }
      counter += word.length
      desc += word
    }
    return desc + '"'
  }

  /**
   * Helper method to make sure added blocks have the correct shadows
   * We still want those in case people decide to pull apart the converted stuff
   * @param workspace The workspace
   * @param type The block type to create
   * @returns The created block
   */
  static newBlock(workspace, type) {
    let block = workspace.newBlock(type)
    this.manuallyAddShadowBlocks(block, Blockly.Blocks[type].json)
    return block
  }

  static colorToHex(colour) {
    var hue = Number(colour)
    if (!isNaN(hue)) {
      return Blockly.hueToHex(hue)
    } else {
      return Blockly.utils.colour.parse(colour)
    }
  }

  static secondaryColor(color) {
    return Blockly.blockRendering.ConstantProvider.prototype.generateSecondaryColour_(color)
  }

  static tertiaryColor(color) {
    return Blockly.blockRendering.ConstantProvider.prototype.generateTertiaryColour_(color)
  }

  static loadableInit(Blockly) {
    if (!Blockly.Css.injected_) {
      Blockly.Css.register([
        '.blocklyCommentTextarea {',
          'color: black;',
          'caret-color: black;',
          'font-size: 12pt;',
          'background-color: lightgray;',
        '}',

        '.blocklyTooltipDiv {',
          'opacity: 1;',
          'font-size: 10pt;',
        '}',

        '.blackText {',
          'fill: #000 !important;',
        '}'
      ]);
    }

    setTimeout(() => {
      const all = Blockly.Workspace.getAll()
      for (let i = 0; i < all.length; i++) {
        const workspace = all[i]
        if (!workspace.parentWorkspace && workspace.rendered) {
          Blockly.svgResize(workspace)
        }
      }
    }, 1)
  }
}