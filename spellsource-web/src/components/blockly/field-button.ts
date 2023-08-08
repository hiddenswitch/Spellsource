import { BlockSvg, Field, utils } from "blockly";

export class FieldButton extends Field {
  static OnClicks: Record<string, (field: FieldButton) => void> = {};

  protected onClickName: string;

  constructor(value: string, onClickName: string, opt_config?: Object) {
    super(value, null, opt_config);
    this.onClickName = onClickName;
  }

  static fromJson(options) {
    return new FieldButton(options["text"], options["function"], options);
  }

  initView() {
    super.initView();

    this.textElement_.textContent = this.getValue();

    const blockColor = utils.colour.parse(this.getSourceBlock().getColour());

    const color = utils.colour.blend(blockColor, "#FFFFFF", 0.75);

    this.borderRect_.setAttribute("fill", color);

    if (this.sourceBlock_.isInFlyout) {
      return;
    }

    this.fieldGroup_.style.cursor = "pointer";

    this.textElement_.onclick = (ev) => {
      FieldButton.OnClicks[this.onClickName]?.(this);
    };
  }

  protected render_() {
    super.render_();
    this.fieldGroup_.classList.remove("blocklyEditableText");
  }
}
