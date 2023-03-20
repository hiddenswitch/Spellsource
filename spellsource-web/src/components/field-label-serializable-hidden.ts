import { FieldLabelSerializable } from 'blockly'

export class FieldLabelSerializableHidden extends FieldLabelSerializable {
  constructor (opt_value: any, opt_class?: string, opt_config?: Object) {
    super(opt_value, opt_class, opt_config)
  }

  static fromJson (options) {
    const field = new FieldLabelSerializableHidden(options.value)
    // field.setValue(options.value)
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