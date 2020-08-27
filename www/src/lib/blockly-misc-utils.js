import Blockly, {FieldLabel} from 'blockly'
import JsonConversionUtils from './json-conversion-utils'
import {has} from 'lodash'
import recursiveOmitBy from 'recursive-omit-by'
import {FieldLabelSerializableHidden} from '../components/field-label-serializable-hidden'
import {FieldLabelPlural} from "../components/field-label-plural";

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
        return 'Condition'
      case 'spell':
      case 'spell1':
      case 'spell2':
      case 'deathrattle':
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
    if (card.baseManaCost !== null) {
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

  static initializeBlocks(data) {
    try {
      Blockly.fieldRegistry.register('field_label_serializable_hidden', FieldLabelSerializableHidden)
      Blockly.fieldRegistry.register('field_label_plural', FieldLabelPlural)
      this.blocklyModification()
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

    BlocklyMiscUtils.initCardBlocks(data)


    let defaultFunction = Blockly.Field.prototype.setValue;
    Blockly.Field.prototype.setValue = function (newValue) {
      defaultFunction.call(this, newValue)
      let source = this.sourceBlock_
      if (!!source && !!Blockly.Blocks[source.type].json) {
        let json = Blockly.Blocks[source.type].json
        if (json.output === 'Color') {
          let r = source.getFieldValue('r')
          let g = source.getFieldValue('g')
          let b = source.getFieldValue('b')
          let color = Blockly.utils.colour.rgbToHex(r, g, b)
          source.setColour(color)
        }
      }
    }
  }

  static getHeroClassColors(data) {
    const newHeroClassColors = {
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

        newHeroClassColors[card.heroClass] = color
        let block = {
          'type': type,
          'message0': card.name,
          'output': 'HeroClass',
          'colour': color,
          'data': card.heroClass
        }
        BlocklyMiscUtils.addBlock(block)
      }
    })
    return newHeroClassColors
  }

  static initCardBlocks(data) {
    const heroClassColors = BlocklyMiscUtils.getHeroClassColors(data)
    //second pass through to actually get the cards
    data.allCard.edges.forEach(edge => {
      let card = edge.node
      let type = 'CatalogueCard_' + card.id
      if (has(Blockly.Blocks, type)) {
        return
      }
      if (heroClassColors.hasOwnProperty(card.heroClass)) { //this check if it's *really* collectible
        let color = heroClassColors[card.heroClass]
        let block = {
          'type': type,
          'args0': [],
          'message0': BlocklyMiscUtils.cardMessage(card),
          'output': 'Card',
          'colour': color,
          'data': card.id,
          'comment': this.cardDescription(card)
        }
        BlocklyMiscUtils.addBlock(block)
      }
    })
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


  static blocklyModification() {
    Blockly.HSV_SATURATION = .65
    Blockly.Blocks = {} //we don't use any of the default Blockly blocks

    //use 2 half-width spacing rows instead of 1 full-width for the inner rows of blocks
    Blockly.blockRendering.RenderInfo.prototype.addRowSpacing_ = function () {
      let oldRows = this.rows
      this.rows = []

      for (let r = 0; r < oldRows.length; r++) {
        this.rows.push(oldRows[r])
        if (r !== oldRows.length - 1) {
          let spacerRow = this.makeSpacerRow_(oldRows[r], oldRows[r + 1])
          if (r !== oldRows.length - 2 && r !== 0) {
            spacerRow.height = spacerRow.height / 2

            let spacerRow2 = this.makeSpacerRow_(oldRows[r], oldRows[r + 1])
            spacerRow2.height = spacerRow2.height / 2
            this.rows.push(spacerRow2)
          }
          this.rows.push(spacerRow)
        }
      }
    }
    //now every single important row has a spacer or equivalent both above and below

    Blockly.blockRendering.RenderInfo.prototype.alignRowElements_ = function () {
      const Types = Blockly.blockRendering.Types
      //align statement rows normally and align input rows to nearest 10 pixels
      for (let i = 0, row; (row = this.rows[i]); i++) {
        if (row.hasStatement) {
          this.alignStatementRow_(row)
        }
        if (row.hasExternalInput && row.width > 1) {
          let happyWidth
          if (row.width < 50) {
            happyWidth = Math.ceil(row.width / 10) * 10
          } else {
            happyWidth = Math.round(row.width / 10) * 10
          }
          let missingSpace = happyWidth - row.width
          this.addAlignmentPadding_(row, missingSpace)
        }
        if (this.block_.hat && i === 2 && row.width < this.topRow.width) {
          let missingSpace = this.topRow.width - row.width
          this.addAlignmentPadding_(row, missingSpace)
        }
      }
      //spacer/top/bottom rows take on the width of their adjacent non-spacer row
      for (let i = 0, row; (row = this.rows[i]); i++) {
        if (Types.isSpacer(row) || Types.isTopOrBottomRow(row)) {
          let currentWidth = row.width
          let desiredWidth = 0

          if (Types.isSpacer(row)) {
            let aboveRow = this.rows[i + 1]
            let belowRow = this.rows[i - 1]
            if (!!aboveRow && !Types.isSpacer(aboveRow) && !Types.isTopOrBottomRow(aboveRow)) {
              desiredWidth = aboveRow.width
            }
            if (!!belowRow && !Types.isSpacer(belowRow) && !Types.isTopOrBottomRow(belowRow)) {
              desiredWidth = belowRow.width
            }
          } else if (Types.isTopRow(row)) {
            desiredWidth = this.rows[2].width
          } else if (Types.isBottomRow(row)) {
            desiredWidth = this.rows[i - 2].width
          }

          let missingSpace = desiredWidth - currentWidth
          if (missingSpace > 0) {
            this.addAlignmentPadding_(row, missingSpace)
          }
          if (Types.isTopOrBottomRow(row)) {
            row.widthWithConnectedBlocks = row.width
          }
        }
      }
    }

    Blockly.BlockSvg.prototype.bumpNeighbours = function () {
      if (!this.workspace) {
        return  // Deleted block.
      }
      if (this.workspace.isDragging()) {
        return  // Don't bump blocks during a drag.
      }
      var rootBlock = this.getRootBlock();
      if (rootBlock.isInFlyout) {
        return  // Don't move blocks around in a flyout.
      }
      // Loop through every connection on this block.
      var myConnections = this.getConnections_(false);
      for (var i = 0, connection; (connection = myConnections[i]); i++) {
        if (autoDecorate(this, connection)) {
          return
        }
        // Spider down from this block bumping all sub-blocks.
        if (connection.isConnected() && connection.isSuperior()) {
          connection.targetBlock().bumpNeighbours()
        }

        var neighbours = connection.neighbours(Blockly.SNAP_RADIUS);


        for (var j = 0, otherConnection; (otherConnection = neighbours[j]); j++) {

          // If both connections are connected, that's probably fine.  But if
          // either one of them is unconnected, then there could be confusion.
          if (!connection.isConnected() || !otherConnection.isConnected()) {
            if (otherConnection.getSourceBlock().getRootBlock() !== rootBlock) {
              // Always bump the inferior block.
              if (connection.isSuperior()) {
                otherConnection.bumpAwayFrom(connection)
              } else {
                connection.bumpAwayFrom(otherConnection)
              }
            }
          }
        }
      }
    };

    const autoDecorate = function (bumpee, connection) {
      if (connection.type === Blockly.NEXT_STATEMENT || bumpee.type.endsWith('SHADOW')) {
        return false
      }
      let workspace = bumpee.workspace
      let nexts = workspace.connectionDBList[Blockly.NEXT_STATEMENT];
      let neighbours = nexts.getNeighbours(connection, Blockly.SNAP_RADIUS)

      for (var j = 0, otherConnection; (otherConnection = neighbours[j]); j++) {
        if ((!connection.isConnected() || connection.targetBlock().isShadow())
          && (!otherConnection.isConnected() || otherConnection.targetBlock().isShadow())
          && otherConnection.type === Blockly.NEXT_STATEMENT) {
          let bumper = otherConnection.getSourceBlock()
          let addedBlock
          if (otherConnection.getCheck().includes('Properties')) {
            if (bumpee.type.startsWith('Aura_')) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_auras')
              addedBlock.getFirstStatementConnection().connect(bumpee.previousConnection)
            } else if (bumpee.type.startsWith('Spell')) {
              if (bumper.getInput('description')?.connection
                .targetBlock()?.getFieldValue('text')?.toLowerCase().includes('aftermath')) {
                addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_aftermath')
                addedBlock.getInput('deathrattle').connection.connect(bumpee.outputConnection)
              } else {
                addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_opener1')
                addedBlock.getInput('battlecry.spell').connection.connect(bumpee.outputConnection)
              }
            } else if (bumpee.type.startsWith('TargetSelection')) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_opener1')
              addedBlock.getInput('battlecry.targetSelection').connection.connect(bumpee.outputConnection)
            } else if (bumpee.type.startsWith('ValueProvider')) {
              if (bumper.getInput('description')?.connection
                .targetBlock()?.getFieldValue('text')?.toLowerCase().includes('if')) {
                addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_cost_modifier_conditional')
                addedBlock.getInput('manaCostModifier.ifTrue').connection.connect(bumpee.outputConnection)
              } else {
                addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_cost_modifier')
                addedBlock.getInput('manaCostModifier').connection.connect(bumpee.outputConnection)
              }
            } else if (bumpee.type.startsWith('Condition')) {
              if (bumper.getInput('description')?.connection
                .targetBlock()?.getFieldValue('text')?.toLowerCase().includes('opener')) {
                addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_opener2')
                addedBlock.getInput('battlecry.condition').connection.connect(bumpee.outputConnection)
              } else {
                addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_condition')
                addedBlock.getInput('condition').connection.connect(bumpee.outputConnection)
              }
            } else if (bumpee.type.startsWith('Property_attributes_')) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_attributes')
              addedBlock.getFirstStatementConnection().connect(bumpee.previousConnection)
            } else if (bumpee.type.startsWith('Attribute')) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_attributes')
              let anotherBlock
              if (bumpee.json?.output === 'IntAttribute') {
                anotherBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_attributes_int')
              } else {
                anotherBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_attributes_boolean')
              }
              anotherBlock.getInput('attribute').connection.connect(bumpee.outputConnection)
              if (anotherBlock.initSvg) {
                anotherBlock.initSvg()
              }
              addedBlock.getFirstStatementConnection().connect(anotherBlock.previousConnection)
            } else if (bumpee.type.startsWith('Enchantment')) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_triggers')
              addedBlock.getFirstStatementConnection().connect(bumpee.previousConnection)
            } else if (bumpee.type.startsWith('Trigger')) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_triggers')
              let anotherBlock = BlocklyMiscUtils.newBlock(workspace, 'Enchantment')
              anotherBlock.getInput('eventTrigger').connection.connect(bumpee.outputConnection)
              if (anotherBlock.initSvg) {
                anotherBlock.initSvg()
              }
              addedBlock.getFirstStatementConnection().connect(anotherBlock.previousConnection)
            } else {
              continue
            }
          } else if (otherConnection.getCheck().includes('Property_attributes') && bumpee.type.startsWith('Attribute_')) {
            if (bumpee.json?.output === 'IntAttribute') {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_attributes_int')
            } else {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, 'Property_attributes_boolean')
            }
            addedBlock.getInput('attribute').connection.connect(bumpee.outputConnection)
          } else if (((otherConnection.getCheck().includes('Spells') && bumpee.type.startsWith('Spell_'))
            || (otherConnection.getCheck().includes('Cards') && bumpee.type.startsWith('Card_'))
            || (otherConnection.getCheck().includes('Conditions') && bumpee.type.startsWith('Condition_'))
            || (otherConnection.getCheck().includes('Sources') && bumpee.type.startsWith('Source_'))
            || (otherConnection.getCheck().includes('Filters') && bumpee.type.startsWith('Filter_')))
            && !bumpee.type.endsWith('I')) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, bumpee.type.split('_')[0] + '_I')
            addedBlock.getInput('i').connection.connect(bumpee.outputConnection)
          } else {
            continue
          }

          if (!!addedBlock) {
            otherConnection.connect(addedBlock.previousConnection)
            if (addedBlock.initSvg) {
              addedBlock.initSvg()
              workspace.render()
            }
            return true
          }
        }
      }

      return false
    }


    const createIcon = Blockly.Icon.prototype.createIcon;
    Blockly.Icon.prototype.createIcon = function () {
      createIcon.call(this)
      Blockly.bindEvent_(
        this.iconGroup_, 'mouseenter', this, () => {
          if (this.block_.isInFlyout) {
            if (this.isVisible()) {
              return
            }
            Blockly.Events.fire(
              new Blockly.Events.Ui(this.block_, 'commentOpen', false, true));
            this.model_.pinned = true;
            this.createNonEditableBubble_();
          }
        });


      Blockly.bindEvent_(
        this.iconGroup_, 'mouseleave', this, () => {
          if (this.block_.isInFlyout) {
            if (!this.isVisible()) {
              return
            }
            Blockly.Events.fire(
              new Blockly.Events.Ui(this.block_, 'commentOpen', true, false));
            this.model_.pinned = false;
            this.disposeBubble_();
          }
        });
    }

    const placeNewBlock = Blockly.Flyout.prototype.placeNewBlock_
    Blockly.Flyout.prototype.placeNewBlock_ = function (oldBlock) {
      let block = placeNewBlock.call(this, oldBlock)
      const removeComments = function (block) {
        block.setCommentText(null)
        for (let childBlock of block.childBlocks_) {
          removeComments(childBlock)
        }
      }
      removeComments(block)
      return block
    }

    const getInRowSpacing = Blockly.geras.RenderInfo.prototype.getInRowSpacing_
    Blockly.geras.RenderInfo.prototype.getInRowSpacing_ = function(prev, next) {
      // Spacing between two fields of the same editability.

      if (prev && Blockly.blockRendering.Types.isField(prev) &&
        next && Blockly.blockRendering.Types.isField(next) &&
        prev.isEditable === next.isEditable &&
        (prev.field instanceof FieldLabelPlural && !(next.field instanceof FieldLabelPlural))) {
        return this.constants_.MEDIUM_PADDING
      }
      if (prev && Blockly.blockRendering.Types.isField(prev) &&
        prev.field instanceof FieldLabelPlural && prev.field.value_ === ' ') {
        return 0
      }
      if (next && Blockly.blockRendering.Types.isField(next) &&
        next.field instanceof FieldLabelPlural &&
        (next.field.value_ === ' ' || next.field.value_ === 's')) {
        if (prev?.field instanceof FieldLabel) {
          return 0
        }
        return 3
      }
      return getInRowSpacing.call(this, prev, next)
    }
  }
}