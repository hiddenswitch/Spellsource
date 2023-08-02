import Blockly, {
  Block,
  BlockSvg,
  CollapsibleToolboxCategory,
  FieldLabel,
  Toolbox,
  ToolboxCategory,
  utils,
  WorkspaceSvg,
} from "blockly";
import { FieldLabelPlural } from "../components/field-label-plural";
import { FieldLabelSerializableHidden } from "../components/field-label-serializable-hidden";
import * as BlocklyMiscUtils from "./blockly-misc-utils";
import * as DefaultOverrides from "./default-overrides";
import React from "react";
import { Field } from "blockly/core/renderers/measurables/field";
import { getBlock } from "./blockly-toolbox";

export function modifyAll() {
  Blockly.utils.colour.setHsvSaturation(0.65);

  for (let blocksKey in Blockly.Blocks) {
    let block = Blockly.Blocks[blocksKey];
    if (!block.init) {
      delete Blockly.Blocks[blocksKey];
    }
  }

  autoDecoration();
  // hoverComments()
  pluralSpacing();
  tooltips();
  textColor();
  colorfulColors();
  noToolboxZoom();
  multiline();
  // jsonShadows()
  connections();
  categoryIndenting();
  // cardDisplay()
  flyout();
  mobileCategories();

  const refreshSelection = Blockly.Toolbox.prototype.refreshSelection;
  Blockly.Toolbox.prototype.refreshSelection = function () {
    const contents = this.selectedItem_?.getContents();

    if (contents) {
      for (let i = 0; i < contents.length; i++) {
        if (contents[i]?.kind === "block" && !contents[i].inputs) {
          contents[i] = getBlock(contents[i].type);
        }
      }
    }

    return refreshSelection.call(this);
  };

  DefaultOverrides.overrideAll();
}

function autoDecoration() {
  Blockly.BlockSvg.prototype.bumpNeighbours = function () {
    if (!this.workspace) {
      return; // Deleted block.
    }
    var rootBlock = this.getRootBlock();
    if (rootBlock.isInFlyout) {
      return; // Don't move blocks around in a flyout.
    }
    // Loop through every connection on this block.
    var myConnections = this.getConnections_(false);
    for (var i = 0, connection; (connection = myConnections[i]); i++) {
      if (autoDecorate(this, connection)) {
        return;
      }
      // Spider down from this block bumping all sub-blocks.
      if (connection.isConnected() && connection.isSuperior()) {
        connection.targetBlock().bumpNeighbours();
      }

      var neighbours = connection.neighbours(Blockly.config.snapRadius);

      for (var j = 0, otherConnection; (otherConnection = neighbours[j]); j++) {
        // If both connections are connected, that's probably fine.  But if
        // either one of them is unconnected, then there could be confusion.
        if (!connection.isConnected() || !otherConnection.isConnected()) {
          if (otherConnection.getSourceBlock().getRootBlock() !== rootBlock) {
            // Always bump the inferior block.
            if (connection.isSuperior()) {
              otherConnection.bumpAwayFrom(connection);
            } else {
              connection.bumpAwayFrom(otherConnection);
            }
          }
        }
      }
    }

    if (this.workspace.isDragging()) {
      return; // Don't bump blocks during a drag.
    }
  };

  const autoDecorate = function (bumpee, connection) {
    if (connection.type === Blockly.NEXT_STATEMENT || bumpee.type.endsWith("SHADOW")) {
      return false;
    }
    let workspace = bumpee.workspace;
    let nexts = workspace.connectionDBList[Blockly.NEXT_STATEMENT];
    let neighbours = nexts.getNeighbours(connection, Blockly.config.snapRadius);

    for (var j = 0, otherConnection; (otherConnection = neighbours[j]); j++) {
      if (
        (!connection.isConnected() || connection.targetBlock().isShadow()) &&
        (!otherConnection.isConnected() || otherConnection.targetBlock().isShadow()) &&
        otherConnection.type === Blockly.NEXT_STATEMENT
      ) {
        let bumper = otherConnection.getSourceBlock();
        let addedBlock;
        if (!otherConnection.getCheck()) {
          continue;
        }

        if (otherConnection.getCheck().includes("Properties")) {
          if (bumpee.type.startsWith("Aura_")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_auras");
            addedBlock.getFirstStatementConnection().connect(bumpee.previousConnection);
          } else if (bumpee.type.startsWith("Spell")) {
            if (
              bumper
                .getInput("description")
                ?.connection.targetBlock()
                ?.getFieldValue("text")
                ?.toLowerCase()
                .includes("aftermath")
            ) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_aftermath");
              addedBlock.getInput("deathrattle").connection.connect(bumpee.outputConnection);
            } else {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_opener1");
              addedBlock.getInput("battlecry.spell").connection.connect(bumpee.outputConnection);
            }
          } else if (bumpee.type.startsWith("TargetSelection")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_opener1");
            addedBlock.getInput("battlecry.targetSelection").connection.connect(bumpee.outputConnection);
          } else if (bumpee.type.startsWith("ValueProvider")) {
            if (
              bumper
                .getInput("description")
                ?.connection.targetBlock()
                ?.getFieldValue("text")
                ?.toLowerCase()
                .includes("if")
            ) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_cost_modifier_conditional");
              addedBlock.getInput("manaCostModifier.ifTrue").connection.connect(bumpee.outputConnection);
            } else {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_cost_modifier");
              addedBlock.getInput("manaCostModifier").connection.connect(bumpee.outputConnection);
            }
          } else if (bumpee.type.startsWith("Condition")) {
            if (
              bumper
                .getInput("description")
                ?.connection.targetBlock()
                ?.getFieldValue("text")
                ?.toLowerCase()
                .includes("opener")
            ) {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_opener2");
              addedBlock.getInput("battlecry.condition").connection.connect(bumpee.outputConnection);
            } else {
              addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_condition");
              addedBlock.getInput("condition").connection.connect(bumpee.outputConnection);
            }
          } else if (bumpee.type.startsWith("Property_attributes_")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes");
            addedBlock.getFirstStatementConnection().connect(bumpee.previousConnection);
          } else if (bumpee.type.startsWith("Attribute")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes");
            let anotherBlock;
            if (bumpee.json?.output === "IntAttribute") {
              anotherBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes_int");
            } else {
              anotherBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes_boolean");
            }
            anotherBlock.getInput("attribute").connection.connect(bumpee.outputConnection);
            if (anotherBlock.initSvg) {
              anotherBlock.initSvg();
            }
            addedBlock.getFirstStatementConnection().connect(anotherBlock.previousConnection);
          } else if (bumpee.type.startsWith("Enchantment")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_triggers");
            addedBlock.getFirstStatementConnection().connect(bumpee.previousConnection);
          } else if (bumpee.type.startsWith("Trigger")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_triggers");
            let anotherBlock = BlocklyMiscUtils.newBlock(workspace, "Enchantment");
            anotherBlock.getInput("eventTrigger").connection.connect(bumpee.outputConnection);
            if ("initSvg" in anotherBlock) {
              anotherBlock.initSvg();
            }
            addedBlock.getFirstStatementConnection().connect(anotherBlock.previousConnection);
          } else if (bumpee.type.startsWith("Art_")) {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_sprite");
            addedBlock.getInput("art.sprite.named").connection.connect(bumpee.outputConnection);
          } else {
            continue;
          }
        } else if (otherConnection.getCheck().includes("Property_attributes") && bumpee.type.startsWith("Attribute_")) {
          if (bumpee.json?.output === "IntAttribute") {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes_int");
          } else {
            addedBlock = BlocklyMiscUtils.newBlock(workspace, "Property_attributes_boolean");
          }
          addedBlock.getInput("attribute").connection.connect(bumpee.outputConnection);
        } else if (
          ((otherConnection.getCheck().includes("Spells") && bumpee.type.startsWith("Spell_")) ||
            (otherConnection.getCheck().includes("Cards") && bumpee.type.startsWith("Card_")) ||
            (otherConnection.getCheck().includes("Conditions") && bumpee.type.startsWith("Condition_")) ||
            (otherConnection.getCheck().includes("Sources") && bumpee.type.startsWith("Source_")) ||
            (otherConnection.getCheck().includes("Filters") && bumpee.type.startsWith("Filter_"))) &&
          !bumpee.type.endsWith("I")
        ) {
          addedBlock = BlocklyMiscUtils.newBlock(workspace, bumpee.type.split("_")[0] + "_I");
          addedBlock.getInput("i").connection.connect(bumpee.outputConnection);
        } else {
          continue;
        }

        if (!!addedBlock) {
          otherConnection.connect(addedBlock.previousConnection);
          if (addedBlock.initSvg) {
            addedBlock.initSvg();
            workspace.render();
          }
          return true;
        }
      }
    }

    return false;
  };
}

/*function hoverComments() {
  const createIcon = Blockly.Icon.prototype.createIcon;
  Blockly.Icon.prototype.createIcon = function () {
    createIcon.call(this);
    Blockly.bindEvent_(this.iconGroup_, "mouseenter", this, () => {
      if (this.block_.isInFlyout) {
        if (this.isVisible()) {
          return;
        }
        Blockly.Events.fire(new Blockly.Events.Ui(this.block_, "commentOpen", false, true));
        this.model_.pinned = true;
        this.createNonEditableBubble_();
      }
    });

    Blockly.bindEvent_(this.iconGroup_, "mouseleave", this, () => {
      if (this.block_.isInFlyout) {
        if (!this.isVisible()) {
          return;
        }
        Blockly.Events.fire(new Blockly.Events.Ui(this.block_, "commentOpen", true, false));
        this.model_.pinned = false;
        this.disposeBubble_();
      }
    });
  };

  const placeNewBlock = Blockly.Flyout.prototype["placeNewBlock_"];
  Blockly.Flyout.prototype["placeNewBlock_"] = function (oldBlock) {
    let block = placeNewBlock.call(this, oldBlock);
    const removeComments = function (block) {
      block.setCommentText(null);
      for (let childBlock of block.childBlocks_) {
        removeComments(childBlock);
      }
    };
    removeComments(block);
    return block;
  };
}*/

function pluralSpacing() {
  const getInRowSpacing = Blockly.geras.RenderInfo.prototype.getInRowSpacing_;
  Blockly.geras.RenderInfo.prototype.getInRowSpacing_ = function (prev, next) {
    // Spacing between two fields of the same editability.

    if (
      prev &&
      Blockly.blockRendering.Types.isField(prev) &&
      next &&
      Blockly.blockRendering.Types.isField(next) &&
      (prev as Field).isEditable === (next as Field).isEditable &&
      (prev as Field).field instanceof FieldLabelPlural &&
      !((next as Field).field instanceof FieldLabelPlural)
    ) {
      return this.constants_.MEDIUM_PADDING;
    }
    if (
      prev &&
      Blockly.blockRendering.Types.isField(prev) &&
      (prev as Field).field instanceof FieldLabelPlural &&
      (prev as Field).field.getValue() === " "
    ) {
      return 0;
    }
    if (
      next &&
      Blockly.blockRendering.Types.isField(next) &&
      (next as Field).field instanceof FieldLabelPlural &&
      ((next as Field).field.getValue() === " " || (next as Field).field.getValue() === "s")
    ) {
      if ((prev as Field)?.field instanceof FieldLabel) {
        return 0;
      }
      return 3;
    }
    if (
      prev &&
      Blockly.blockRendering.Types.isField(prev) &&
      next &&
      Blockly.blockRendering.Types.isField(next) &&
      (prev as Field).isEditable === (next as Field).isEditable &&
      !((prev as Field).field instanceof FieldLabelPlural) &&
      (next as Field).field instanceof FieldLabelPlural &&
      !((prev as Field).field instanceof FieldLabelSerializableHidden)
    ) {
      return this.constants_.MEDIUM_PADDING;
    }
    return getInRowSpacing.call(this, prev, next);
  };
}

function tooltips() {
  // @ts-ignore
  // noinspection JSConstantReassignment
  Blockly.Tooltip.OFFSET_X = 10;
  if (Blockly.Touch.TOUCH_ENABLED) {
    // @ts-ignore
    // noinspection JSConstantReassignment
    Blockly.Tooltip.OFFSET_X = 50;
  }
  // @ts-ignore
  // noinspection JSConstantReassignment
  Blockly.Tooltip.OFFSET_Y = 0;

  const init = Blockly.ToolboxCategory.prototype.init;
  Blockly.ToolboxCategory.prototype.init = function () {
    init.call(this);
    if (this.toolboxItemDef_["tooltip"]) {
      this.rowDiv_.tooltip = this.toolboxItemDef_["tooltip"];
      this.rowDiv_.tooltipColor = this.colour_;
      Blockly.Tooltip.bindMouseEvents(this.rowDiv_);
    }
  };

  /*Blockly.Tooltip.bindMouseEvents = function (element) {
    if (Blockly.Touch.TOUCH_ENABLED) {
      element["mouseOverWrapper_"] = Blockly.bindEvent_(element, 'touchstart', null,
        Blockly.Tooltip["onMouseOver_"]);
      element["mouseOutWrapper_"] = Blockly.bindEvent_(element, 'touchend', null,
        Blockly.Tooltip["onMouseOut_"]);
      element.addEventListener('touchstart', Blockly.Tooltip["onMouseMove_"], false);
    } else {
      element["mouseOverWrapper_"] = Blockly.bindEvent_(element, 'mouseover', null,
        Blockly.Tooltip["onMouseOver_"]);
      element["mouseOutWrapper_"] = Blockly.bindEvent_(element, 'mouseout', null,
        Blockly.Tooltip["onMouseOut_"]);
      element.addEventListener('mousemove', Blockly.Tooltip["onMouseMove_"], false);
    }
  };*/

  const show = Blockly.Tooltip["show_"];
  Blockly.Tooltip["show_"] = function () {
    show.call(this);
    if (!!Blockly.Tooltip["DIV"]) {
      if (!!Blockly.Tooltip["element_"].tooltipColor) {
        Blockly.Tooltip["DIV"]["style"].backgroundColor = Blockly.Tooltip["element_"].tooltipColor;
        Blockly.Tooltip["DIV"]["style"].color = "#ffffff";
      } else {
        Blockly.Tooltip["DIV"]["style"].backgroundColor = "#ffffc7";
        Blockly.Tooltip["DIV"]["style"].color = "#000";
      }
    }
  };
}

function textColor() {
  const createTextElement = Blockly.Field.prototype["createTextElement_"];
  Blockly.Field.prototype["createTextElement_"] = function () {
    createTextElement.call(this);
    let block = this.getSourceBlock();
    const typeTextColor = Blockly["textColor"]?.[block.type];
    const idTextColor = Blockly["textColor"]?.[block.getFieldValue("id")];
    const color = typeTextColor ?? idTextColor;
    if (color && block.type.endsWith("_REFERENCE")) {
      this.textElement_.style.fill = color;
    }
  };
}

function colorfulColors() {
  let defaultFunction = Blockly.Field.prototype.setValue;
  Blockly.Field.prototype.setValue = function (newValue) {
    defaultFunction.call(this, newValue);
    let source = this.sourceBlock_;
    if (!!source && !!Blockly.Blocks[source.type].json) {
      let json = Blockly.Blocks[source.type].json;
      if (json.output === "Color") {
        let r = source.getFieldValue("r");
        let g = source.getFieldValue("g");
        let b = source.getFieldValue("b");
        let color = Blockly.utils.colour.rgbToHex(r, g, b);
        source.setColour(color);
      }
    }
    if (source?.type === "variables_get" || source?.type == "variables_get_dynamic") {
      let type = this.variable_?.type;
      if (type === "EntityReference") {
        source.setColour(30);
      } else {
        source.setColour(source?.type === "variables_get" ? "#a53a6f" : "#a53a93");
      }
    }
  };
}

function noToolboxZoom() {
  const layout = Blockly.HorizontalFlyout.prototype["layout_"];
  Blockly.HorizontalFlyout.prototype["layout_"] = function (contents, gaps) {
    const workspace = this.targetWorkspace as WorkspaceSvg;
    if (workspace) {
      const reset = workspace.scale;
      workspace.scale = workspace.options.zoomOptions.startScale;
      layout.call(this, contents, gaps);
      workspace.scale = reset;
    } else {
      layout.call(this, contents, gaps);
    }
  };

  const reflowInternal = Blockly.HorizontalFlyout.prototype["reflowInternal_"];
  Blockly.HorizontalFlyout.prototype["reflowInternal_"] = function () {
    const workspace = this.targetWorkspace as WorkspaceSvg;
    if (workspace) {
      const reset = workspace.scale;
      workspace.scale = workspace.options.zoomOptions.startScale;
      reflowInternal.call(this);
      workspace.scale = reset;
    } else {
      reflowInternal.call(this);
    }
  };

  const layout2 = Blockly.VerticalFlyout.prototype["layout_"];
  Blockly.VerticalFlyout.prototype["layout_"] = function (contents, gaps) {
    const workspace = this.targetWorkspace as WorkspaceSvg;
    if (workspace) {
      const reset = workspace.scale;
      workspace.scale = workspace.options.zoomOptions.startScale;
      layout2.call(this, contents, gaps);
      workspace.scale = reset;
    } else {
      layout2.call(this, contents, gaps);
    }
  };

  const reflowInternal2 = Blockly.VerticalFlyout.prototype["reflowInternal_"];
  Blockly.VerticalFlyout.prototype["reflowInternal_"] = function () {
    const workspace = this.targetWorkspace as WorkspaceSvg;
    if (workspace) {
      const reset = workspace.scale;
      workspace.scale = workspace.options.zoomOptions.startScale;
      reflowInternal2.call(this);
      workspace.scale = reset;
    } else {
      reflowInternal2.call(this);
    }
  };
}

function multiline() {
  Blockly.FieldMultilineInput.prototype["getDisplayText_"] = function () {
    let value = this.value_;
    if (!value) {
      // Prevent the field from disappearing if empty.
      return Blockly.Field.NBSP;
    }

    if (value.length < this.maxDisplayLength) {
      return value.replaceAll(/\s/g, Blockly.Field.NBSP);
    }

    let text = "";
    let words = value.replaceAll("\n", "").split(" ");
    let i = 0;
    for (let wordsKey of words) {
      if (i + wordsKey.length + 1 > this.maxDisplayLength) {
        text += "\n";
        text += wordsKey;
        text += Blockly.Field.NBSP;
        i = 0;
      } else {
        text += wordsKey;
        text += Blockly.Field.NBSP;
      }
      i += wordsKey.length + 1;
    }

    return text;
  };

  // Fix multiline texts having wrong width in toolbox
  const updateSize = Blockly.FieldMultilineInput.prototype["updateSize_"];
  Blockly.FieldMultilineInput.prototype["updateSize_"] = function () {
    updateSize.call(this);

    if (this.size_.width === 10) {
      const fontSize = this.getConstants().FIELD_TEXT_FONTSIZE;
      const fontWeight = this.getConstants().FIELD_TEXT_FONTWEIGHT;
      const fontFamily = this.getConstants().FIELD_TEXT_FONTFAMILY;

      const nodes = this.textGroup_.childNodes;
      for (let i = 0; i < nodes.length; i++) {
        const tspan = nodes[i];
        const textWidth = Blockly.utils.dom.getFastTextWidth(tspan, fontSize, fontWeight, fontFamily);
        if (textWidth > this.size_.width) {
          this.size_.width = textWidth;
        }
      }
      if (this.borderRect_) {
        this.size_.width += this.getConstants().FIELD_BORDER_RECT_X_PADDING * 2;
        this.borderRect_.setAttribute("width", this.size_.width);
      }

      this.positionBorderRect_();
    }
  };
}

function connections() {
  const setCheck = Blockly.Connection.prototype.setCheck;
  Blockly.Connection.prototype.setCheck = function (check) {
    const ret = setCheck.call(this, check);
    if (check === "Boolean") {
      this.check_?.push("ConditionDesc");
    }
    return ret;
  };
}

// Make nested category indent themselves
function categoryIndenting() {
  const createRowContainer = Blockly.ToolboxCategory.prototype["createRowContainer_"];

  Blockly.ToolboxCategory.prototype["createRowContainer_"] = function () {
    const rowDiv = createRowContainer.call(this);
    let nestedPadding = Blockly.ToolboxCategory.nestedPadding * this.getLevel();
    rowDiv.style.paddingLeft = 0;
    rowDiv.style.marginLeft = (nestedPadding / 2).toFixed(0) + "px";

    return rowDiv;
  };
}

/*function cardDisplay() {
  const createBubble = Blockly.Comment.prototype["createBubble_"];

  Blockly.Comment.prototype["createBubble_"] = function () {
    createBubble.call(this);

    let block = this.block_ as BlockSvg;
    if (block.type.startsWith("Starter_")) {
      this.textarea_.remove();
      this.foreignObject_.previousElementSibling.remove();
      let card = JSON.parse(block.getCommentText()) as CardDef;

      /!*if (!!card.art?.sprite?.named && !!block.artURL) {
        card.art.sprite.named = block.artURL
      }*!/

      if (card.heroClass) {
        const heroClassBlock = Blockly.Blocks["HeroClass_" + card.heroClass];
        const heroClass = heroClassBlock?.json?.json;
        card.art = deepmerge(heroClass?.art || {}, card.art || {});
      }

      /!*createRoot(this.foreignObject_.firstElementChild).render(<CardDisplay {...card} />);*!/
    }
  };
}*/

// Uncollapse blocks when you take them out of the toolbox
function flyout() {
  const unCollapse = (block: Block) => {
    if (!block) return;
    block.setCollapsed(false);
    block.getChildren(true).forEach(unCollapse);
  };

  const createBlock = Blockly.Flyout.prototype.createBlock;
  Blockly.Flyout.prototype.createBlock = function (originalBlock) {
    const result: BlockSvg = createBlock.call(this, originalBlock);
    if (result.type.startsWith("Starter_")) {
      unCollapse(result);
    }
    return result;
  };
}

// Make categories on mobile have borders on different sides
function mobileCategories() {
  ToolboxCategory.prototype["addColourBorder_"] = function (colour) {
    const style = this.rowDiv_.style;
    style.border = ToolboxCategory.borderWidth + "px solid " + (colour || "#ddd");
  };

  const setExpanded = CollapsibleToolboxCategory.prototype.setExpanded;
  CollapsibleToolboxCategory.prototype.setExpanded = function (isExpanded) {
    setExpanded.call(this, isExpanded);

    const category = this as CollapsibleToolboxCategory;
    const toolbox = category["parentToolbox_"] as Toolbox;
    const htmlDiv = toolbox.HtmlDiv;
    utils.style.scrollIntoContainerView(category.getDiv(), htmlDiv, !isExpanded);
  };
}
