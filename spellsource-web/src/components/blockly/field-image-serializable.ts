import { FieldImage } from "blockly";
import { Field } from "blockly/core/field";
import { FieldImageConfig } from "blockly/core/field_image";

export class FieldImageSerializable extends FieldImage {
  static type = "field_image_serializable";

  constructor(
    src: string | typeof Field.SKIP_SETUP,
    width: string | number,
    height: string | number,
    alt?: string,
    onClick?: (p1: FieldImage) => void,
    flipRtl?: boolean,
    config?: FieldImageConfig
  ) {
    super(src, width, height, alt, onClick, flipRtl, config);
    this.SERIALIZABLE = true;
  }
}
