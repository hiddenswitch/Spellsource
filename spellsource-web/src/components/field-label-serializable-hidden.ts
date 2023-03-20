import { FieldLabelSerializable } from 'blockly'

export class FieldLabelSerializableHidden extends FieldLabelSerializable {
  constructor (opt_value, opt_validator, opt_config) {
    super(opt_value, opt_validator, opt_config)
  }

  static fromJson (options) {
    const field = new FieldLabelSerializableHidden()
    field.setValue(options.value)
    return field
  }

  getSize () {
    let size = super.getSize()
    size.width = -10
    return size
  }

  getDisplayText_ () {
    return ''
  }
}