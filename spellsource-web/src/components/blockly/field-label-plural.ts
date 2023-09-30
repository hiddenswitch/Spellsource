import { FieldLabel } from "blockly";

export class FieldLabelPlural extends FieldLabel {
  static type = "field_label_plural";

  constructor(opt_value?: string, opt_class?: string, opt_config?: Object) {
    super(opt_value, opt_class, opt_config);
  }

  static fromJson(
    options: {
      value?: string;
      text?: string;
    } & object
  ) {
    const field = new FieldLabelPlural();
    if (options.value) {
      field.setValue(options.value);
    } else {
      field.setValue(options.text);
    }
    return field;
  }
}
