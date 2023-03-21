import Blockly from "blockly"
import {BlockArgDef} from "./blocks";

export default class DefaultOverrides {

  static overrideAll() {
    this.math()
    this.loops()
    this.text()
  }

  static numShadow(type, name, value = null) {
    this.addShadow(type, name, 'math_number', 'NUM', value)
  }

  static textShadow(type, name, value = null) {
    this.addShadow(type, name, 'text', 'TEXT', value)
  }

  static addShadow(type, name, shadowType, fieldName = null, value = null) {
    if (!Blockly.Blocks[type].json) {
      Blockly.Blocks[type].json = {
        "args0": []
      }
    }
    let arg: BlockArgDef = {
      name,
      shadow: {
        type: shadowType
      }
    }
    if (!!fieldName) {
      arg.shadow.fields = [
        {
          "name": fieldName
        }
      ]
      if (typeof value === 'number') {
        arg.shadow.fields[0].valueI = value
      } else if (typeof value === 'string') {
        arg.shadow.fields[0].valueS = value
      } else if (value === true || value === false) {
        arg.shadow.fields[0].valueB = value
      }
    }
    Blockly.Blocks[type].json["args0"].push(arg)
  }

  static math() {
    this.numShadow('math_arithmetic', 'A', 1)
    this.numShadow('math_arithmetic', 'B', 1)

    this.numShadow('math_single', 'NUM',  9)

    this.numShadow('math_trig', 'NUM',  45)

    this.numShadow('math_number_property', 'NUMBER_TO_CHECK')

    this.numShadow('math_round', 'NUM', 3.1)

    this.numShadow('math_modulo', 'DIVIDEND',64)
    this.numShadow('math_modulo', 'DIVISOR',10)

    this.numShadow('math_constrain', 'VALUE',50)
    this.numShadow('math_constrain', 'LOW', 1)
    this.numShadow('math_constrain', 'HIGH', 100)

    this.numShadow('math_random_int', 'FROM', 1)
    this.numShadow('math_random_int', 'TO', 100)

    this.numShadow('math_atan2', 'X', 3)
    this.numShadow('math_atan2', 'Y', 4)
  }

  static loops() {
    this.numShadow('controls_repeat', 'TIMES', 10)

    this.numShadow('controls_for', 'FROM', 1)
    this.numShadow('controls_for', 'TO', 10)
    this.numShadow('controls_for', 'BY', 1)
  }

  static text() {
    this.textShadow('text_append', 'TEXT')

    this.textShadow('text_length', 'VALUE', 'abc')

    this.textShadow('text_isEmpty', 'VALUE')

    this.textShadow('text_indexOf', 'FIND', 'abc')

    this.textShadow('text_changeCase', 'TEXT', 'abc')

    this.textShadow('text_trim', 'TEXT', 'abc')

    this.textShadow('text_print', 'TEXT', 'abc')

    this.textShadow('text_count', 'TEXT', 'abc')
    this.textShadow('text_count', 'SUB', 'a')

    this.textShadow('text_replace', 'TEXT', 'abc')
    this.textShadow('text_replace', 'FROM', 'abc')
    this.textShadow('text_replace', 'TO', 'cba')

    this.textShadow('text_reverse', 'TEXT', 'abc')
  }
}
