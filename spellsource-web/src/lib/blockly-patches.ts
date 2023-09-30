import Blockly, { Block, BlockSvg, FieldLabel, RenderedConnection, ToolboxCategory } from "blockly";
import { FieldLabelPlural } from "../components/blockly/field-label-plural";
import { FieldHidden } from "../components/blockly/field-hidden";
import { refreshBlock } from "./blockly-spellsource-utils";
import * as DefaultOverrides from "./default-overrides";
import { Field } from "blockly/core/renderers/measurables/field";
import { newBlock } from "./blockly-utils";

export function modifyAll() {
  Blockly.utils.colour.setHsvSaturation(0.65);

  for (let blocksKey in Blockly.Blocks) {
    let block = Blockly.Blocks[blocksKey];
    if (!block.init) {
      delete Blockly.Blocks[blocksKey];
    }
  }

  // autoDecoration();
  // hoverComments()
  pluralSpacing();
  tooltips();
  textColor();
  // colorfulColors();
  multiline();
  // jsonShadows()
  // connections();
  categoryIndenting();
  // cardDisplay()
  flyout();
  mobileCategories();

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
        connection.targetBlock()?.bumpNeighbours();
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

  const autoDecorate = function (bumpee: BlockSvg, connection: RenderedConnection) {
    if (connection.type === Blockly.NEXT_STATEMENT || bumpee.type.endsWith("SHADOW")) {
      return false;
    }
    let workspace = bumpee.workspace;
    let nexts = workspace.connectionDBList[Blockly.NEXT_STATEMENT];
    let neighbours = nexts.getNeighbours(connection, Blockly.config.snapRadius);

    for (var j = 0, otherConnection; (otherConnection = neighbours[j]); j++) {
      if (
        (!connection.isConnected() || connection.targetBlock()?.isShadow()) &&
        (!otherConnection.isConnected() || otherConnection.targetBlock()?.isShadow()) &&
        otherConnection.type === Blockly.NEXT_STATEMENT
      ) {
        let bumper = otherConnection.getSourceBlock();
        let addedBlock;
        if (!otherConnection.getCheck()) {
          continue;
        }

        if (otherConnection.getCheck()?.includes("Properties")) {
          if (bumpee.type.startsWith("Aura_")) {
            addedBlock = newBlock(workspace, "Property_auras");
            addedBlock.getFirstStatementConnection()?.connect(bumpee.previousConnection);
          } else if (bumpee.type.startsWith("Spell")) {
            if (
              bumper
                .getInput("description")
                ?.connection?.targetBlock()
                ?.getFieldValue("text")
                ?.toLowerCase()
                .includes("aftermath")
            ) {
              addedBlock = newBlock(workspace, "Property_aftermath");
              addedBlock.getInput("deathrattle")?.connection?.connect(bumpee.outputConnection);
            } else {
              addedBlock = newBlock(workspace, "Property_opener1");
              addedBlock.getInput("battlecry.spell")?.connection?.connect(bumpee.outputConnection);
            }
          } else if (bumpee.type.startsWith("TargetSelection")) {
            addedBlock = newBlock(workspace, "Property_opener1");
            addedBlock.getInput("battlecry.targetSelection")?.connection?.connect(bumpee.outputConnection);
          } else if (bumpee.type.startsWith("ValueProvider")) {
            if (
              bumper
                .getInput("description")
                ?.connection?.targetBlock()
                ?.getFieldValue("text")
                ?.toLowerCase()
                .includes("if")
            ) {
              addedBlock = newBlock(workspace, "Property_cost_modifier_conditional");
              addedBlock.getInput("manaCostModifier.ifTrue")?.connection?.connect(bumpee.outputConnection);
            } else {
              addedBlock = newBlock(workspace, "Property_cost_modifier");
              addedBlock.getInput("manaCostModifier")?.connection?.connect(bumpee.outputConnection);
            }
          } else if (bumpee.type.startsWith("Condition")) {
            if (
              bumper
                .getInput("description")
                ?.connection?.targetBlock()
                ?.getFieldValue("text")
                ?.toLowerCase()
                .includes("opener")
            ) {
              addedBlock = newBlock(workspace, "Property_opener2");
              addedBlock.getInput("battlecry.condition")?.connection?.connect(bumpee.outputConnection);
            } else {
              addedBlock = newBlock(workspace, "Property_condition");
              addedBlock.getInput("condition")?.connection?.connect(bumpee.outputConnection);
            }
          } else if (bumpee.type.startsWith("Property_attributes_")) {
            addedBlock = newBlock(workspace, "Property_attributes");
            addedBlock.getFirstStatementConnection()?.connect(bumpee.previousConnection);
          } else if (bumpee.type.startsWith("Attribute")) {
            addedBlock = newBlock(workspace, "Property_attributes");
            let anotherBlock;
            if (bumpee.json?.output === "IntAttribute") {
              anotherBlock = newBlock(workspace, "Property_attributes_int");
            } else {
              anotherBlock = newBlock(workspace, "Property_attributes_boolean");
            }
            anotherBlock.getInput("attribute")?.connection?.connect(bumpee.outputConnection);
            if ("initSvg" in anotherBlock && typeof anotherBlock["initSvg"] === "function") {
              anotherBlock.initSvg();
            }
            addedBlock.getFirstStatementConnection()?.connect(anotherBlock.previousConnection!);
          } else if (bumpee.type.startsWith("Enchantment")) {
            addedBlock = newBlock(workspace, "Property_triggers");
            addedBlock.getFirstStatementConnection()?.connect(bumpee.previousConnection);
          } else if (bumpee.type.startsWith("Trigger")) {
            addedBlock = newBlock(workspace, "Property_triggers");
            let anotherBlock = newBlock(workspace, "Enchantment");
            anotherBlock.getInput("eventTrigger")?.connection?.connect(bumpee.outputConnection);
            if ("initSvg" in anotherBlock) {
              anotherBlock.initSvg();
            }
            addedBlock.getFirstStatementConnection()?.connect(anotherBlock.previousConnection!);
          } else if (bumpee.type.startsWith("Art_")) {
            addedBlock = newBlock(workspace, "Property_sprite");
            addedBlock.getInput("art.sprite.named")?.connection?.connect(bumpee.outputConnection);
          } else {
            continue;
          }
        } else if (
          otherConnection.getCheck()?.includes("Property_attributes") &&
          bumpee.type.startsWith("Attribute_")
        ) {
          if (bumpee.json?.output === "IntAttribute") {
            addedBlock = newBlock(workspace, "Property_attributes_int");
          } else {
            addedBlock = newBlock(workspace, "Property_attributes_boolean");
          }
          addedBlock.getInput("attribute")?.connection?.connect(bumpee.outputConnection);
        } else if (
          ((otherConnection.getCheck()?.includes("Spells") && bumpee.type.startsWith("Spell_")) ||
            (otherConnection.getCheck()?.includes("Cards") && bumpee.type.startsWith("Card_")) ||
            (otherConnection.getCheck()?.includes("Conditions") && bumpee.type.startsWith("Condition_")) ||
            (otherConnection.getCheck()?.includes("Sources") && bumpee.type.startsWith("Source_")) ||
            (otherConnection.getCheck()?.includes("Filters") && bumpee.type.startsWith("Filter_"))) &&
          !bumpee.type.endsWith("I")
        ) {
          addedBlock = newBlock(workspace, bumpee.type.split("_")[0] + "_I");
          addedBlock.getInput("i")?.connection?.connect(bumpee.outputConnection);
        } else {
          continue;
        }

        if (addedBlock) {
          otherConnection.connect(addedBlock.previousConnection!);
          if ("initSvg" in addedBlock && typeof addedBlock["initSvg"] === "function") {
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
      !((prev as Field).field instanceof FieldHidden)
    ) {
      return this.constants_.MEDIUM_PADDING;
    }
    return getInRowSpacing.call(this, prev, next);
  };
}

type RowDivWithTooltip = HTMLDivElement & {
  tooltip: unknown;
  tooltipColor: string;
};

function tooltips() {
  // @ts-ignore
  // noinspection JSConstantReassignment
  const BlocklyTooltip = Blockly.Tooltip as any;
  BlocklyTooltip.OFFSET_X = 10;
  if (Blockly.Touch.TOUCH_ENABLED) {
    // @ts-ignore
    // noinspection JSConstantReassignment
    BlocklyTooltip.OFFSET_X = 50;
  }
  // @ts-ignore
  // noinspection JSConstantReassignment
  BlocklyTooltip.OFFSET_Y = 0;

  const init = Blockly.ToolboxCategory.prototype.init;
  Blockly.ToolboxCategory.prototype.init = function () {
    init.call(this);
    if ("tooltip" in this.toolboxItemDef_) {
      const rowDiv = this["rowDiv_"] as RowDivWithTooltip;
      rowDiv.tooltip = this.toolboxItemDef_["tooltip"];
      rowDiv.tooltipColor = this["colour_"];
      BlocklyTooltip.bindMouseEvents(rowDiv);
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

  const show = BlocklyTooltip["show_"];
  BlocklyTooltip["show_"] = function () {
    show.call(this);
    if (BlocklyTooltip["DIV"]) {
      if (BlocklyTooltip["element_"].tooltipColor) {
        BlocklyTooltip["DIV"]["style"].backgroundColor = BlocklyTooltip["element_"].tooltipColor;
        BlocklyTooltip["DIV"]["style"].color = "#ffffff";
      } else {
        BlocklyTooltip["DIV"]["style"].backgroundColor = "#ffffc7";
        BlocklyTooltip["DIV"]["style"].color = "#000";
      }
    }
  };
}

function textColor() {
  const blockly = Blockly as any;
  const createTextElement = blockly.Field.prototype["createTextElement_"];
  blockly.Field.prototype["createTextElement_"] = function () {
    createTextElement.call(this);
    let block = this.getSourceBlock();
    const typeTextColor = blockly["textColor"]?.[block.type];
    const idTextColor = blockly["textColor"]?.[block.getFieldValue("id")];
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
    let source = this["sourceBlock_"];
    if (source && Blockly.Blocks[source.type].json) {
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
      let type =
        "variable_" in this &&
        typeof this["variable_"] === "object" &&
        this["variable_"] != null &&
        "type" in this["variable_"]
          ? this["variable_"]["type"]
          : "";
      if (type === "EntityReference") {
        source.setColour(30);
      } else {
        source.setColour(source?.type === "variables_get" ? "#a53a6f" : "#a53a93");
      }
    }
  };
}

function multiline() {
  Blockly.FieldMultilineInput.prototype["getDisplayText_"] = function () {
    let value = this["value_"];
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

    const size = this["size_"];
    if (size.width === 10) {
      const constantProvider = this.getConstants()!;
      const fontSize = constantProvider.FIELD_TEXT_FONTSIZE;
      const fontWeight = constantProvider.FIELD_TEXT_FONTWEIGHT;
      const fontFamily = constantProvider.FIELD_TEXT_FONTFAMILY;

      const nodes = this.textGroup?.childNodes ?? [];
      for (let i = 0; i < nodes.length; i++) {
        const tspan = nodes[i];
        // todo: is this really an svg text element?
        const textWidth = Blockly.utils.dom.getFastTextWidth(tspan as SVGTextElement, fontSize, fontWeight, fontFamily);
        if (textWidth > size.width) {
          size.width = textWidth;
        }
      }
      if (this["borderRect_"]) {
        size.width += constantProvider.FIELD_BORDER_RECT_X_PADDING * 2;
        this["borderRect_"].setAttribute("width", size.width.toString());
      }

      this["positionBorderRect_"]();
    }
  };
}

function connections() {
  const setCheck = Blockly.Connection.prototype.setCheck;
  Blockly.Connection.prototype.setCheck = function (check) {
    const ret = setCheck.call(this, check);
    if (check === "Boolean") {
      this["check"].push("ConditionDesc");
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
    rowDiv.style.paddingLeft = "0";
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

      /!*if (card.art?.sprite?.named && block.artURL) {
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

  // Ensure blocks updated for toolbox
  const refreshSelection = Blockly.Toolbox.prototype.refreshSelection;
  Blockly.Toolbox.prototype.refreshSelection = function () {
    refreshSelection.call(this);
    Blockly.Workspace.getAll()
      .filter((workspace) => workspace.isFlyout)
      .forEach((workspace) => workspace.getAllBlocks(false).forEach(refreshBlock));
  };
}

// Make categories on mobile have borders on different sides
function mobileCategories() {
  ToolboxCategory.prototype["addColourBorder_"] = function (colour) {
    const style = this["rowDiv_"]?.style;
    if (style) {
      style.border = ToolboxCategory.borderWidth + "px solid " + (colour || "#ddd");
    }
  };
}
