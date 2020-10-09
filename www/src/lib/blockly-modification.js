import Blockly, {FieldLabel, Workspace} from "blockly";
import {FieldLabelPlural} from "../components/field-label-plural";
import {FieldLabelSerializableHidden} from "../components/field-label-serializable-hidden";
import JsonConversionUtils from "./json-conversion-utils";
import BlocklyMiscUtils from "./blockly-misc-utils";
import DefaultOverrides from "./default-overrides";
import SpellsourceGenerator from "./spellsource-generator";

export default class BlocklyModification {

  static modifyAll() {
    Blockly.HSV_SATURATION = .65

    for (let blocksKey in Blockly.Blocks) {
      let block = Blockly.Blocks[blocksKey]
      if (!block.init) {
        delete Blockly.Blocks[blocksKey]
      }
    }
    Blockly.Blocks['math_change'].testKey = 'variable_change'
    Blockly.Blocks['controls_if'].testKey = 'logic_if'
    Blockly.Blocks['controls_ifelse'].testKey = 'logic_ifelse'

    this.autoDecoration()
    //this.hoverComments()
    this.pluralSpacing()
    this.contextMenu()
    this.tooltips()
    this.blackText()
    this.colorfulColors()
    this.noToolboxZoom()
    this.multiline()

    DefaultOverrides.overrideAll()
  }

  static autoDecoration() {
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
          if (!otherConnection.getCheck()) {
            continue
          }

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
  }

  static hoverComments() {
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
  }

  static pluralSpacing() {
    const getInRowSpacing = Blockly.geras.RenderInfo.prototype.getInRowSpacing_
    Blockly.geras.RenderInfo.prototype.getInRowSpacing_ = function (prev, next) {
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
      if (prev && Blockly.blockRendering.Types.isField(prev) &&
        next && Blockly.blockRendering.Types.isField(next) &&
        prev.isEditable === next.isEditable &&
        (!(prev.field instanceof FieldLabelPlural) && next.field instanceof FieldLabelPlural)
        && !(prev.field instanceof FieldLabelSerializableHidden)) {
        return this.constants_.MEDIUM_PADDING
      }
      return getInRowSpacing.call(this, prev, next)
    }
  }

  static contextMenu() {
    const generateContextMenu = Blockly.BlockSvg.prototype.generateContextMenu
    Blockly.BlockSvg.prototype.generateContextMenu = function () {
      let menuOptions = generateContextMenu.call(this)
      let block = this
      if (block.type.startsWith('CatalogueCard')) {
        menuOptions.push({
          text: 'Import CardScript',
          enabled: true,
          callback: function () {
            if (!!block.json && !!block.json.json) {
              let card = JSON.parse(block.json.json)
              let dummyWorkspace = new Workspace()
              JsonConversionUtils.generateCard(dummyWorkspace, card)

              let top = dummyWorkspace.getTopBlocks(false)[0]

              let xml = Blockly.Xml.blockToDom(top, true);
              var xy = top.getRelativeToSurfaceXY();
              xml.setAttribute('x', xy.x);
              xml.setAttribute('y', xy.y);

              let workspace = block.workspace
              if (!!workspace.targetWorkspace) {
                workspace = workspace.targetWorkspace
              }

              workspace.paste(xml)
              dummyWorkspace.dispose()
              workspace.getToolbox().clearSelection()

            }
          }
        })
      }

      return menuOptions.filter(option => option.enabled)
    }

  }

  static tooltips() {
    if (!Blockly.categoryTooltips) {
      Blockly.categoryTooltips = {}
    }
    if (!Blockly.tooltipColors) {
      Blockly.tooltipColors = {}
    }

    Blockly.Tooltip.OFFSET_X = 10;
    if (Blockly.Touch.TOUCH_ENABLED) {
      Blockly.Tooltip.OFFSET_X = 50;
    }
    Blockly.Tooltip.OFFSET_Y = 0;

    const getRowDom = Blockly.tree.BaseNode.prototype.getRowDom
    Blockly.tree.BaseNode.prototype.getRowDom = function () {
      const element = getRowDom.call(this)
      let name = this.content
      if (!!this.parent_?.content) {
        name = this.parent_.content + '.' + name
      }
      if (!!name && !!Blockly.categoryTooltips[name]) {
        element.tooltip = Blockly.categoryTooltips[name]
        element.tooltipColor = Blockly.tooltipColors[name]
        Blockly.Tooltip.bindMouseEvents(element)
      }
      return element
    }

    Blockly.Tooltip.bindMouseEvents = function (element) {
      if (Blockly.Touch.TOUCH_ENABLED) {
        element.mouseOverWrapper_ = Blockly.bindEvent_(element, 'touchstart', null,
          Blockly.Tooltip.onMouseOver_);
        element.mouseOutWrapper_ = Blockly.bindEvent_(element, 'touchend', null,
          Blockly.Tooltip.onMouseOut_);
        element.addEventListener('touchstart', Blockly.Tooltip.onMouseMove_, false);
      } else {
        element.mouseOverWrapper_ = Blockly.bindEvent_(element, 'mouseover', null,
          Blockly.Tooltip.onMouseOver_);
        element.mouseOutWrapper_ = Blockly.bindEvent_(element, 'mouseout', null,
          Blockly.Tooltip.onMouseOut_);
        element.addEventListener('mousemove', Blockly.Tooltip.onMouseMove_, false);
      }
    };

    const show = Blockly.Tooltip.show_
    Blockly.Tooltip.show_ = function() {
      show.call(this)
      if (!!Blockly.Tooltip.DIV) {
        if (!!Blockly.Tooltip.element_.tooltipColor) {
          Blockly.Tooltip.DIV.style.backgroundColor = Blockly.Tooltip.element_.tooltipColor
          Blockly.Tooltip.DIV.style.color = '#ffffff'
        } else {
          Blockly.Tooltip.DIV.style.backgroundColor = '#ffffc7'
          Blockly.Tooltip.DIV.style.color = '#000'
        }
      }
    }
  }

  static blackText() {
    const createTextElement = Blockly.Field.prototype.createTextElement_
    Blockly.Field.prototype.createTextElement_ = function() {
      createTextElement.call(this)
      if (!!Blockly.textColor && Blockly.textColor[this.getSourceBlock().colour_]) {
        this.textElement_.style.fill = Blockly.textColor[this.getSourceBlock().colour_]
      }
    }
  }

  static colorfulColors() {
    let defaultFunction = Blockly.Field.prototype.setValue
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

  static noToolboxZoom() {
    const layout2 = Blockly.VerticalFlyout.prototype.layout_
    Blockly.VerticalFlyout.prototype.layout_ = function(contents, gaps) {
      if (!!this.targetWorkspace_) {
        const reset = this.targetWorkspace_.scale
        this.targetWorkspace_.scale = 1.0
        layout2.call(this, contents, gaps)
        this.targetWorkspace_.scale = reset
      } else {
        layout2.call(this, contents, gaps)
      }
    }

    const reflowInternal2 = Blockly.VerticalFlyout.prototype.reflowInternal_
    Blockly.VerticalFlyout.prototype.reflowInternal_ = function() {
      if (!!this.targetWorkspace_) {
        const reset = this.targetWorkspace_.scale
        this.targetWorkspace_.scale = 1.0
        reflowInternal2.call(this)
        this.targetWorkspace_.scale = reset
      } else {
        reflowInternal2.call(this)
      }
    }
  }

  static multiline() {
    Blockly.FieldMultilineInput.prototype.getDisplayText_ = function() {
      let value = this.value_
      if (!value) {
        // Prevent the field from disappearing if empty.
        return Blockly.Field.NBSP
      }

      if (value.length < this.maxDisplayLength) {
        return value.replaceAll(/\s/g, Blockly.Field.NBSP);
      }

      let text = ''
      let words = value.replaceAll('\n', '').split(' ')
      let i = 0
      for (let wordsKey of words) {
        if (i + wordsKey.length + 1 > this.maxDisplayLength) {
          text += '\n'
          text += wordsKey
          text += Blockly.Field.NBSP
          i = 0
        } else {
          text += wordsKey
          text += Blockly.Field.NBSP
        }
        i += wordsKey.length + 1
      }

      return text
    }
  }
}