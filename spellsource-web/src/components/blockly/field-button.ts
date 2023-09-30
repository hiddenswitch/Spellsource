import { Field, utils, WorkspaceSvg } from "blockly";

export class FieldButton extends Field {
  static type = "field_button";

  protected callbackKey: string;

  constructor(value: string, callbackKey: string, opt_config?: Object) {
    super(value, null, opt_config);
    this.callbackKey = callbackKey;
    this.SERIALIZABLE = false;
    this.EDITABLE = false;
  }

  static fromJson(
    options: {
      text: string;
      callbackKey: string;
    } & object
  ) {
    return new FieldButton(options["text"], options["callbackKey"], options);
  }

  setValue(newValue: any, fireChangeEvent?: boolean) {
    if (this.textElement_) {
      this.textElement_.textContent = newValue;
      this.markDirty();
    }
    super.setValue(newValue, fireChangeEvent);
  }

  initView() {
    super.initView();

    this.textElement_!.textContent = this.getValue();

    const blockColor = utils.colour.parse(this.getSourceBlock()!.getColour());

    const color = utils.colour.blend(blockColor!, "#FFFFFF", 0.75);

    this.borderRect_!.setAttribute("fill", color!);

    if (this.sourceBlock_!.isInFlyout) {
      return;
    }

    this.fieldGroup_!.style.cursor = "pointer";

    this.textElement_!.onclick = (ev) => {
      const workspace = this.getSourceBlock()!.workspace as WorkspaceSvg;
      const buttonCallback = workspace.getButtonCallback(this.callbackKey);

      buttonCallback?.(this as any);
    };
  }

  protected render_() {
    super.render_();
    this.fieldGroup_?.classList.remove("blocklyEditableText");
  }
}
