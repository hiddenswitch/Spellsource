import Blockly, {FieldLabel} from 'blockly'
import {FieldLabelPlural} from "../components/field-label-plural";
import {FieldLabelSerializableHidden} from "../components/field-label-serializable-hidden";

const SpellsourceRenderer = function (name) {
  SpellsourceRenderer.superClass_.constructor.call(this, name)
}
Blockly.utils.object.inherits(SpellsourceRenderer,
  Blockly.geras.Renderer);

SpellsourceRenderer.prototype.makeRenderInfo_ = function(block) {
  return new SpellsourceRenderInfo(this, block);
};


const SpellsourceRenderInfo = function (renderer, block) {
  SpellsourceRenderInfo.superClass_.constructor.call(this, renderer, block)
}
Blockly.utils.object.inherits(SpellsourceRenderInfo,
  Blockly.geras.RenderInfo);


//use 2 half-width spacing rows instead of 1 full-width for the inner rows of blocks
SpellsourceRenderInfo.prototype.addRowSpacing_ = function () {
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
SpellsourceRenderInfo.prototype.alignRowElements_ = function () {
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
    if ((this.block_.hat || this.block_.json?.hat) && i === 2 && row.width < this.topRow.width) {
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

export default SpellsourceRenderer