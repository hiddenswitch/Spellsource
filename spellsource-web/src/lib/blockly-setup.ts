import { InitBlockOptions } from "../components/card-editor-workspace";
import Blockly, { BlockSvg, CollapsibleToolboxCategory, hasBubble, WorkspaceSvg } from "blockly";
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
import { PlusMinusRows, PlusMinusRowsFn, PlusMinusRowsMixin } from "../components/blockly/plus-minus-rows";
import * as BlocklyContextMenu from "./blockly-context-menu";
import SpellsourceRenderer from "./spellsource-renderer";
import { SpellsourceConnectionChecker } from "../components/blockly/connection-checker";
import { CustomHorizontalFlyout } from "../components/blockly/custom-horizontal-flyout";
import { CustomVerticalFlyout } from "../components/blockly/custom-vertical-flyout";
import { CustomCollapsibleCategory } from "../components/blockly/custom-collapsible-category";
import { generateArt, randomizeSeed } from "./art-generation";
import { FieldImageSerializable } from "../components/blockly/field-image-serializable";
import { ContextType } from "react";
import { BlocklyDataContext } from "../pages/card-editor";
import { workspaceCopy, workspaceCut, workspacePaste } from "./blockly-shortcuts";

export const registerAll = (options?: InitBlockOptions) => {
  Blockly.fieldRegistry.register(FieldHidden.type, FieldHidden);
  Blockly.fieldRegistry.register(FieldLabelPlural.type, FieldLabelPlural);
  Blockly.fieldRegistry.register(FieldButton.type, FieldButton);
  Blockly.fieldRegistry.register(FieldProgressBar.type, FieldProgressBar);
  Blockly.fieldRegistry.register(FieldColourHsvSliders.type, FieldColourHsvSliders);
  Blockly.fieldRegistry.register(FieldPlus.type, FieldPlus);
  Blockly.fieldRegistry.register(FieldMinus.type, FieldMinus);
  Blockly.fieldRegistry.register(FieldImageSerializable.type, FieldImageSerializable);

  Blockly.registry.register(
    Blockly.registry.Type.TOOLBOX_ITEM,
    ToolboxSearchCategory.registrationName,
    ToolboxSearchCategory
  );
  Blockly.registry.register(
    Blockly.registry.Type.TOOLBOX_ITEM,
    CardSearchCategory.registrationName,
    CardSearchCategory
  );
  Blockly.registry.register(
    Blockly.registry.Type.FLYOUTS_HORIZONTAL_TOOLBOX,
    CustomHorizontalFlyout.name,
    CustomHorizontalFlyout
  );
  Blockly.registry.register(
    Blockly.registry.Type.FLYOUTS_VERTICAL_TOOLBOX,
    CustomVerticalFlyout.name,
    CustomVerticalFlyout
  );
  Blockly.registry.unregister(Blockly.registry.Type.TOOLBOX_ITEM, CollapsibleToolboxCategory.registrationName);
  Blockly.registry.register(
    Blockly.registry.Type.TOOLBOX_ITEM,
    CollapsibleToolboxCategory.registrationName,
    CustomCollapsibleCategory
  );

  Blockly.Extensions.registerMutator(OptionalRows, OptionalRowsMixin, OptionalRowsFn);
  Blockly.Extensions.registerMutator(PlusMinusRows, PlusMinusRowsMixin, PlusMinusRowsFn);

  Blockly.ShortcutRegistry.registry.unregister(Blockly.ShortcutItems.names.PASTE);
  Blockly.ShortcutRegistry.registry.register(workspacePaste);
  const ctrlV = Blockly.ShortcutRegistry.registry.createSerializedKey(Blockly.utils.KeyCodes.V, [
    Blockly.utils.KeyCodes.CTRL,
  ]);
  Blockly.ShortcutRegistry.registry.addKeyMapping(ctrlV, Blockly.ShortcutItems.names.PASTE);
  const altV = Blockly.ShortcutRegistry.registry.createSerializedKey(Blockly.utils.KeyCodes.V, [
    Blockly.utils.KeyCodes.ALT,
  ]);
  Blockly.ShortcutRegistry.registry.addKeyMapping(altV, Blockly.ShortcutItems.names.PASTE);

  Blockly.ShortcutRegistry.registry.unregister(Blockly.ShortcutItems.names.COPY);
  Blockly.ShortcutRegistry.registry.register(workspaceCopy);
  const ctrlC = Blockly.ShortcutRegistry.registry.createSerializedKey(Blockly.utils.KeyCodes.C, [
    Blockly.utils.KeyCodes.CTRL,
  ]);
  Blockly.ShortcutRegistry.registry.addKeyMapping(ctrlC, Blockly.ShortcutItems.names.COPY);
  const altC = Blockly.ShortcutRegistry.registry.createSerializedKey(Blockly.utils.KeyCodes.C, [
    Blockly.utils.KeyCodes.ALT,
  ]);
  Blockly.ShortcutRegistry.registry.addKeyMapping(altC, Blockly.ShortcutItems.names.COPY);

  Blockly.ShortcutRegistry.registry.unregister(Blockly.ShortcutItems.names.CUT);
  Blockly.ShortcutRegistry.registry.register(workspaceCut);
  const ctrlX = Blockly.ShortcutRegistry.registry.createSerializedKey(Blockly.utils.KeyCodes.X, [
    Blockly.utils.KeyCodes.CTRL,
  ]);
  Blockly.ShortcutRegistry.registry.addKeyMapping(ctrlX, Blockly.ShortcutItems.names.CUT);

  if (options) {
    BlocklyContextMenu.registerAll(options);
  }

  Blockly.blockRendering.register(SpellsourceRenderer.name, SpellsourceRenderer);

  Blockly.registry.register(
    Blockly.registry.Type.CONNECTION_CHECKER,
    SpellsourceConnectionChecker.name,
    SpellsourceConnectionChecker
  );
};

export const plugins = {
  [Blockly.registry.Type.CONNECTION_CHECKER.toString()]: SpellsourceConnectionChecker.name,
  [Blockly.registry.Type.FLYOUTS_HORIZONTAL_TOOLBOX.toString()]: CustomHorizontalFlyout.name,
  [Blockly.registry.Type.FLYOUTS_VERTICAL_TOOLBOX.toString()]: CustomVerticalFlyout.name,
};

export type WorkspaceSvgWithData = WorkspaceSvg & {
  _data: ContextType<typeof BlocklyDataContext>;
};

export const initWorkspace = (workspace1: WorkspaceSvg, data: ContextType<typeof BlocklyDataContext>) => {
  const workspace = workspace1 as WorkspaceSvgWithData;
  workspace["_data"] = data;
  workspace.registerButtonCallback("generateArt", generateArt);
  workspace.registerButtonCallback("randomizeSeed", randomizeSeed);

  workspace.addChangeListener((event1: Blockly.Events.Abstract) => {
    if (event1.type !== Blockly.Events.CLICK) {
      return;
    }
    const event = event1 as Blockly.Events.Click;

    if (event.targetType !== "block") {
      return;
    }

    const workspace = Blockly.Workspace.getById(event.workspaceId);
    if (!workspace || !event.blockId) {
      return;
    }
    const block = workspace.getBlockById(event.blockId) as BlockSvg & {
      _lastClickTime?: number;
    };

    if (block.isInFlyout || !block.isMovable() || !workspace.options.collapse) return;

    const now = Date.now();

    if (now - (block["_lastClickTime"] ?? 0) < 300) {
      block.setCollapsed(!block.isCollapsed());
    }

    block["_lastClickTime"] = now;
  });

  // Click on workspace to close comment / mutator bubbles
  workspace.addChangeListener((event1: Blockly.Events.Abstract) => {
    if (event1.type !== Blockly.Events.CLICK) {
      return;
    }
    const event = event1 as Blockly.Events.Click;

    if (event.targetType !== "workspace") {
      return;
    }

    const workspace = Blockly.Workspace.getById(event.workspaceId) as WorkspaceSvg;

    if (workspace.isMutator) return;

    workspace.getAllBlocks(false).forEach((block) => {
      for (let icon of block.getIcons()) {
        if (hasBubble(icon) && icon.bubbleIsVisible()) {
          icon.setBubbleVisible(false);
        }
      }
    });
  });

  return workspace as WorkspaceSvgWithData;
};
