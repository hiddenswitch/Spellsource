import { VerticalFlyout } from "blockly";
import { FlyoutDefinition } from "blockly/core/utils/toolbox";

export class CustomVerticalFlyout extends VerticalFlyout {
  getFlyoutScale(): number {
    return 1;
  }

  show(flyoutDef: FlyoutDefinition | string) {
    super.show(flyoutDef);
  }
}
