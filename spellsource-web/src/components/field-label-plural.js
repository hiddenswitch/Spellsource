import { FieldLabel } from 'blockly'
import JsonConversionUtils from "../lib/json-conversion-utils";

export class FieldLabelPlural extends FieldLabel {
  constructor (opt_value, opt_validator, opt_config) {
    super(opt_value, opt_validator, opt_config)
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