import { Field, utils } from "blockly";
import { clamp } from "lodash";

export class FieldProgressBar extends Field {
  protected width: number;
  protected height: number;

  protected bar: SVGRectElement;
  protected background: SVGRectElement;

  constructor(progress: number, width?: number, height?: number, opt_config?: Object) {
    super(progress, null, opt_config);
    this.width = width || 100;
    this.height = height || 15;
  }

  static fromJson(options) {
    return new FieldProgressBar(options["progress"], options["width"], options["height"], options);
  }

  initView() {
    this.createBorderRect_();
    this.background = this.borderRect_;
    this.createBorderRect_();

    this.bar = this.borderRect_;

    this.fieldGroup_.prepend(this.background);

    const blockColor = utils.colour.parse(this.getSourceBlock().getColour());

    const barColor = utils.colour.blend(blockColor, "#FFFFFF", 0.5);
    const backgroundColor = utils.colour.blend(blockColor, "#000000", 0.5);

    this.bar.setAttribute("fill", barColor);
    this.background.setAttribute("fill", backgroundColor);
  }

  protected render_() {
    super.render_();

    this.fieldGroup_.classList.remove("blocklyEditableText");
    this.bar.setAttribute("width", String(this.width * this.getProgress()));
  }

  protected updateSize_(opt_margin?: number | undefined) {
    super.updateSize_(opt_margin);

    this.background.setAttribute("width", String(this.width));
    this.background.setAttribute("height", String(this.height));
    this.bar.setAttribute("height", String(this.height));

    this.size_.width = this.width;
    this.size_.height = this.isVisible() ? this.height : 0;

    /*
    this.bar.setAttribute("x", String(this.getConstants().FIELD_BORDER_RECT_X_PADDING));
    this.background.setAttribute("x", String(this.getConstants().FIELD_BORDER_RECT_X_PADDING));
    this.size_.width = this.width + this.getConstants().FIELD_BORDER_RECT_X_PADDING;
    */
  }

  public getProgress() {
    return this.getValue() as number;
  }

  public setProgress(newProgress: number) {
    this.setValue(newProgress);
  }
}
