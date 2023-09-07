import { KeyboardShortcut } from "blockly/core/shortcut_registry";
import Blockly, { Block, BlockSvg, WorkspaceSvg } from "blockly";
import { generateCard } from "./json-conversion-utils";
import { blockStateToCardScript } from "./workspace-utils";

export const workspacePaste: KeyboardShortcut = {
  name: Blockly.ShortcutItems.names.PASTE,
  preconditionFn: (workspace) => !workspace.options.readOnly && !Blockly.Gesture.inProgress(),
  callback: (workspace: WorkspaceSvg, event) => {
    event.preventDefault();

    navigator.clipboard
      .readText()
      .then((text) => {
        const json = JSON.parse(text);
        if ("type" in json) {
          generateCard(workspace, json);
        } else {
          Blockly.clipboard.paste();
        }
      })
      .catch(() => Blockly.clipboard.paste());

    return true;
  },
};

export const workspaceCopy: KeyboardShortcut = {
  name: Blockly.ShortcutItems.names.COPY,
  preconditionFn(workspace) {
    const selected = Blockly.getSelected();
    return (
      !workspace.options.readOnly &&
      !Blockly.Gesture.inProgress() &&
      (!selected || (selected.isDeletable() && selected.isMovable()))
    );
  },
  callback(workspace: WorkspaceSvg, event) {
    event.preventDefault();
    workspace.hideChaff();

    let selected =
      Blockly.getSelected() ?? workspace.getTopBlocks(true).find((block) => block.type.startsWith("Starter_"));

    if (!selected) return false;

    Blockly.clipboard.copy(selected);

    if (!(selected instanceof Block)) return false;

    const blockState = Blockly.serialization.blocks.save(selected, {
      addCoordinates: true,
    });

    try {
      const cardScript = blockStateToCardScript(blockState);

      navigator.clipboard.writeText(JSON.stringify(cardScript, null, 2));
      return true;
    } catch (e) {
      if (process.env.NODE_ENV !== "production") {
        console.warn(e);
      }
      return false;
    }
  },
};

export const workspaceCut: KeyboardShortcut = {
  name: Blockly.ShortcutItems.names.CUT,
  preconditionFn(workspace) {
    const selected = Blockly.getSelected();
    return (
      !workspace.options.readOnly &&
      !Blockly.Gesture.inProgress() &&
      selected != null &&
      selected instanceof BlockSvg &&
      selected.isDeletable() &&
      selected.isMovable() &&
      !selected.workspace!.isFlyout
    );
  },
  callback(workspace: WorkspaceSvg, event, shortcut) {
    if (!workspaceCopy.callback(workspace, event, shortcut)) return false;

    const selected = Blockly.getSelected();
    if (selected instanceof Block) {
      (selected as BlockSvg).checkAndDelete();
    }

    return true;
  },
};
