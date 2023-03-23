import { FieldLabel } from 'blockly'

export class FieldLabelPlural extends FieldLabel {
  constructor (opt_value?: string, opt_class?: string, opt_config?: Object) {
    super(opt_value, opt_class, opt_config)
  }

  static fromJson (options) {
    const field = new FieldLabelPlural()
    if (options.value) {
      field.setValue(options.value)
    } else {
      field.setValue(options.text)
    }
    return field
  }
}
