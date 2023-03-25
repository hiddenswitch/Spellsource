import Blockly, {Block, BlockSvg} from 'blockly'
import * as JsonConversionUtils from './json-conversion-utils'
import {FieldLabelSerializableHidden} from '../components/field-label-serializable-hidden'
import {FieldLabelPlural} from "../components/field-label-plural";
import * as BlocklyModification from "./blockly-modification";
import {CardDef} from "../components/card-display";
import {BlocklyDataContext} from "../pages/card-editor";
import {ContextType} from "react";
import {BlockDef} from "./blocks";
import {InitBlockOptions} from "../components/card-editor-workspace";

export const toHappyFormatting = (string: string) => string.split('_')
  .map(w => w[0].toUpperCase() + w.substring(1).toLowerCase())
  .join(' ');

export function addBlock(block: BlockDef) {
  Blockly.Blocks[block.type!] = {
    init: function () {
      this.jsonInit(block)
      if (!!block.data) {
        this.data = block.data
      }
      if (!!block.hat) {
        this.hat = block.hat
      }
      if (block.type!.endsWith('SHADOW')) {
        this.setMovable(false)
      }
      if (!!block.comment) {
        this.setTooltip(block.comment)
      }
    },
    json: block
  }
  JsonConversionUtils.addBlockToMap(block)
}

//initializes the json specified shadow blocks of a block on the workspace
export function manuallyAddShadowBlocks(thisBlock: Block, block: Block) {
  for (let i = 0; i < 10; i++) {
    if (!!block['args' + i.toString()]) {
      for (let j = 0; j < 10; j++) {
        const arg = block['args' + i.toString()][j]
        if (!!arg) {
          const shadow = arg.shadow
          if (!!shadow && !thisBlock.getInput(arg.name).connection.targetBlock()) {
            let shadowBlock = newBlock(thisBlock.workspace, shadow.type)
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
            if ("initSvg" in shadowBlock) {
              shadowBlock.initSvg()
            }
            manuallyAddShadowBlocks(shadowBlock, Blockly.Blocks[shadow.type].json)
          }
        }
      }
    }
  }

  if (block.type.startsWith('Starter')) {
    let shadowBlock = thisBlock.workspace.newBlock('Property_SHADOW')
    shadowBlock.setShadow(true)
    thisBlock.nextConnection.connect(shadowBlock.previousConnection)
    if ("initSvg" in shadowBlock) {
      (shadowBlock as BlockSvg).initSvg()
    }
  }
}

export function inputNameToBlockType(inputName: string) {
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

export function blockTypeToOuput(type) {
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
export function cardMessage(card: CardDef) {
  let ret = ''
  if (card.baseManaCost !== null && card.baseManaCost !== undefined) {
    ret = '(' + card.baseManaCost + ') '
  }
  if (card.type === 'MINION') {
    ret += (card.baseAttack ?? 0) + '/' + (card.baseHp ?? 0)
  } else {
    ret += toHappyFormatting(card.type)
  }
  ret += ' "' + card.name + '"'
  return ret
}

export function initBlocks(data: ContextType<typeof BlocklyDataContext>, options: InitBlockOptions) {
  try {
    Blockly.fieldRegistry.register('field_label_serializable_hidden', FieldLabelSerializableHidden)
    Blockly.fieldRegistry.register('field_label_plural', FieldLabelPlural)
    BlocklyModification.modifyAll(options)
  } catch (e) {
    // already registered
  }


  // All of our spells, triggers, entity reference enum values, etc.
  data.allBlocks.forEach(block => {
    if (block.type in Blockly.Blocks) {
      return
    }

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

          if (!!data.allIcons && arg.type === 'field_image' && arg.src && !arg.src.includes('.')) {
            for (let icon of data.allIcons) {
              if (icon.name === arg.src) {
                arg.src = icon.src
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

    addBlock(block)
  })
}

export function initHeroClassColors(data: ContextType<typeof BlocklyDataContext>) {
  if (!Blockly.textColor) {
    Blockly.textColor = {
      'Rarity_COMMON': '#000000'
    }
  }
  Blockly.heroClassColors = {
    ANY: '#A6A6A6'
  }

  /**
   * first pass through the card catalogue to figure out all the collectible
   * hero classes and their colors
   */
  Object.values(data.cardsById).forEach(card => {
    let type = 'HeroClass_' + card.heroClass
    if (type in Blockly.Blocks) {
      return
    }
    if (card.type === 'CLASS' && card.collectible !== false) {
      let color = Blockly.utils.colour.rgbToHex(
        card.art.primary.r * 255,
        card.art.primary.g * 255,
        card.art.primary.b * 255
      )


      Blockly.heroClassColors[card.heroClass] = color
      let block = {
        type,
        message0: card.name,
        output: 'HeroClass',
        colour: color,
        data: card.heroClass,
        json: card
      }
      addBlock(block)
      if (!!card.art.body?.vertex) {
        Blockly.textColor[type] = Blockly.utils.colour.rgbToHex(
          card.art.body.vertex.r * 255,
          card.art.body.vertex.g * 255,
          card.art.body.vertex.b * 25
        )
      }
    }
  })
}

export function initCardBlocks(data: ContextType<typeof BlocklyDataContext>) {
  for (let card of Object.values(data.cardsById)) {
    if (!card.type || card.type === 'FORMAT') {
      continue
    }
    let type = 'CatalogueCard_' + card.id
    if (type in Blockly.Blocks) {
      return
    }
    if (card.heroClass in Blockly.heroClassColors) { //this check if it's *really* collectible
      let color = Blockly.heroClassColors[card.heroClass]
      let block = {
        'type': type,
        'args0': [],
        'message0': cardMessage(card),
        'output': 'Card',
        'colour': color,
        'data': card.id,
        'comment': cardDescription(card),
        'json': card
      }
      addBlock(block)
    } else {
      // console.warn(`${card.id} had invalid hero class`)
    }


    if (!!card.art?.sprite?.named) {
      let name = 'Art_' + card.art.sprite.named
      let artBlock = Blockly.Blocks[name];
      if (!!artBlock) {
        artBlock.used = true
      }
    }
  }
}

export function cardDescription(card: CardDef) {
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
    desc = toHappyFormatting(card.race) + ' ' + desc
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
export function newBlock(workspace, type): Block | BlockSvg {
  let block = workspace.newBlock(type)
  manuallyAddShadowBlocks(block, Blockly.Blocks[type].json)
  return block
}

export function colorToHex(colour) {
  var hue = Number(colour)
  if (!isNaN(hue)) {
    return Blockly.hueToHex(hue)
  } else {
    return Blockly.utils.colour.parse(colour)
  }
}

export function secondaryColor(color) {
  return Blockly.blockRendering.ConstantProvider.prototype.generateSecondaryColour_(color)
}

export function tertiaryColor(color) {
  return Blockly.blockRendering.ConstantProvider.prototype.generateTertiaryColour_(color)
}

export function loadableInit(Blockly) {
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

export function switchRenderer(renderer, workspace) {
  if (!!workspace.render && renderer !== workspace.getRenderer().name) {
    workspace.renderer_ = Blockly.blockRendering.init(renderer,
      workspace.getTheme(), workspace.options.rendererOverrides)

    workspace.getToolbox().getFlyout().getWorkspace().renderer_ = Blockly.blockRendering.init(renderer,
      workspace.getToolbox().getFlyout().getWorkspace().getTheme(), workspace.options.rendererOverrides)

    workspace.refreshTheme()
  }
}

export function pluralStuff(workspace) {
  let anyChange = false
  for (let block of workspace.getAllBlocks()) {
    if (!block.json) {
      continue
    }
    let argsList = JsonConversionUtils.argsList(block.json);
    for (let arg of argsList) {
      if (arg.type === 'field_label_plural') {
        let shouldBePlural = null
        let connection
        //on a plural field, 'src' is where it should look to know whether it's plural or not
        if (arg.src === 'OUTPUT') {
          connection = block.outputConnection
          if (!!block.outputConnection.targetBlock()) {
            let targetBlock = connection.targetBlock()
            if (targetBlock.type.endsWith('_I') && !!targetBlock.getPreviousBlock()) {
              let prevBlock = targetBlock.getPreviousBlock()
              while (!!prevBlock.getPreviousBlock()) {
                prevBlock = prevBlock.getPreviousBlock()
              }
              connection = prevBlock.outputConnection
              targetBlock = connection.targetBlock()
            }
            if (!!targetBlock) { //if the 'src' arg appears on the 'input_value' it's connected to, redirect to that
              let name = targetBlock.getInputWithBlock(block)?.name
              for (let arg of JsonConversionUtils.inputsList(targetBlock.json)) {
                if (arg.name === name && !!arg.src) { //
                  connection = targetBlock.getInput(arg.src).connection
                }
              }
            }
          }
        } else {
          connection = block.getInput(arg.src)?.connection
        }
        if (!connection) {
          if (!!block.getField(arg.src)) {
            shouldBePlural = block.getFieldValue(arg.src) !== 1
          }
        } else if (!!connection.targetBlock()) {
          let targetBlock = connection.targetBlock()
          if (targetBlock.json?.plural != null) {
            shouldBePlural = targetBlock.json.plural
          } else if (targetBlock.type === 'ValueProvider_int') {
            shouldBePlural = targetBlock.getFieldValue('int') !== 1
          }
        }

        let before = block.getFieldValue(arg.name)
        const options = arg.text.split('/')
        if (shouldBePlural === null) {
          if (arg.value) { //on a plural field, 'value' is the default text to show (e.g. in the toolbox)
            block.setFieldValue(arg.value, arg.name)
          } else {
            block.setFieldValue(arg.text, arg.name)
          }
        } else if (shouldBePlural) {
          block.setFieldValue(options[1], arg.name)
        } else {
          block.setFieldValue(options[0], arg.name)
        }

        if (block.getFieldValue(arg.name) !== before) {
          anyChange = true
        }
      }
    }
  }

  if (anyChange) {
    for (let block of workspace.getAllBlocks()) {
      for (var i = 0, input; (input = block.inputList[i]); i++) {
        for (var j = 0, field; (field = input.fieldRow[j]); j++) {
          if (field.isBeingEdited_ && field.showEditor_) {
            field.showEditor_()
          }
        }
      }
    }
  }
}

export function isSpellsourceBlock(type) {
  return !!Blockly.Blocks[type]?.json?.type
}

export function searchToolbox(blockType, mainWorkspace) {
  let toolbox = mainWorkspace.getToolbox()
  let categories = toolbox.getToolboxItems().slice(1)

  for (let category of categories) {
    if (category.getContents) {
      let contents = category.getContents()
      for (let content of contents) {
        if (content.kind === 'block' && content.type === blockType) {
          if (category.getParent() && category.getParent().isCollapsible()) {
            category.getParent().setExpanded(true)
          }
          toolbox.setSelectedItem(category)

          if (toolbox.getFlyout() && toolbox.getFlyout().getWorkspace()) {
            let workspace = toolbox.getFlyout().getWorkspace()
            let totalHeight = 0
            for (let topBlock of workspace.getTopBlocks(true)) {
              if (topBlock.type === blockType) {
                toolbox.getFlyout().scrollbar.set(totalHeight)
                topBlock.addSelect()
              } else {
                totalHeight += topBlock.height + 24
              }
            }
          }

          return
        }
      }
    }
  }
}


export function initArtBlcks(data: ContextType<typeof BlocklyDataContext>) {
  for (let art of data.allArt) {
    let type = 'Art_' + art.name
    if (type in Blockly.Blocks) {
      return
    }
    let block = {
      'type': type,
      'message0': '%1',
      'args0': [
        {
          'type': 'field_image',
          'width': art.width * 1.5,
          'height': art.height * 1.5,
          'src': art.src
        }
      ],
      'output': 'Art',
      'colour': '#A6A6A6',
      'data': art.name,
      'comment': art.name
    }
    addBlock(block)
  }
}


export const refreshBlock = (block: BlockSvg) => {
  block.data = Blockly.Blocks[block.type].data
  block.setFieldValue(Blockly.Blocks[block.type].message, 'message')
  block.setColour(Blockly.Blocks[block.type].json.colour)
  if (block.render) {
    let textElement = block.getSvgRoot().lastElementChild.firstElementChild
    if (Blockly.textColor && Blockly.textColor[block.type]) {
      textElement.style.fill = Blockly.textColor[block.type]
    } else {
      textElement.style.fill = '#fff'
    }
    block.render()
  }
}
