import { InitBlockOptions } from "../components/card-editor-workspace";
import Blockly from "blockly";
import { FieldHidden } from "../components/blockly/field-hidden";
import { FieldLabelPlural } from "../components/blockly/field-label-plural";
import { FieldButton } from "../components/blockly/field-button";
import { FieldProgressBar } from "../components/blockly/field-progress-bar";
import { FieldColourHsvSliders } from "../components/blockly/field-colour-hsv-sliders";
import { FieldPlus } from "../components/blockly/field-plus";
import { FieldMinus } from "../components/blockly/field-minus";
import { ToolboxSearchCategory } from "../components/blockly/toolbox-search-category";
import { CardSearchCategory } from "../components/blockly/card-search-category";
import { OptionalRows, OptionalRowsFn, OptionalRowsMixin } from "../components/blockly/optional-rows";
import { TestExtensionMixin, TestExtensionName } from "../components/blockly/test-extension";
import { PlusMinusRows, PlusMinusRowsFn, PlusMinusRowsMixin } from "../components/blockly/plus-minus-rows";
import * as BlocklyContextMenu from "./blockly-context-menu";
import SpellsourceRenderer from "./spellsource-renderer";
import { SpellsourceConnectionChecker } from "../components/blockly/connection-checker";

export const registerAll = (options?: InitBlockOptions) => {
  Blockly.fieldRegistry.register(FieldHidden.type, FieldHidden);
  Blockly.fieldRegistry.register(FieldLabelPlural.type, FieldLabelPlural);
  Blockly.fieldRegistry.register(FieldButton.type, FieldButton);
  Blockly.fieldRegistry.register(FieldProgressBar.type, FieldProgressBar);
  Blockly.fieldRegistry.register(FieldColourHsvSliders.type, FieldColourHsvSliders);
  Blockly.fieldRegistry.register(FieldPlus.type, FieldPlus);
  Blockly.fieldRegistry.register(FieldMinus.type, FieldMinus);

  Blockly.registry.register(
    Blockly.registry.Type.TOOLBOX_ITEM,
    ToolboxSearchCategory.SEARCH_CATEGORY_KIND,
    ToolboxSearchCategory
  );
  Blockly.registry.register(
    Blockly.registry.Type.TOOLBOX_ITEM,
    CardSearchCategory.SEARCH_CATEGORY_KIND,
    CardSearchCategory
  );

  Blockly.Extensions.registerMutator(OptionalRows, OptionalRowsMixin, OptionalRowsFn);
  Blockly.Extensions.registerMutator(TestExtensionName, TestExtensionMixin);
  Blockly.Extensions.registerMutator(PlusMinusRows, PlusMinusRowsMixin, PlusMinusRowsFn);

  if (options) {
    BlocklyContextMenu.registerAll(options);
  }

  Blockly.blockRendering.register(SpellsourceRenderer.name, SpellsourceRenderer);

  FieldButton.OnClicks["test"] = (field) => {
    const progressBar = field.getSourceBlock().getField("progress") as FieldProgressBar;
    progressBar.setProgress(Math.random());
  };

  Blockly.registry.register(
    Blockly.registry.Type.CONNECTION_CHECKER,
    SpellsourceConnectionChecker.name,
    SpellsourceConnectionChecker
  );
};

export const plugins = {
  [Blockly.registry.Type.CONNECTION_CHECKER.toString()]: SpellsourceConnectionChecker.name,
};
