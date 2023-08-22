import { HorizontalFlyout } from "blockly";

export class CustomHorizontalFlyout extends HorizontalFlyout {
  getFlyoutScale(): number {
    return 1;
  }
}
