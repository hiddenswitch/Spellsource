import { ExtensionMixin } from "./mutators";
import Blockly, { Block } from "blockly";

interface TestExtensionState {
  inputCounter: number;
  minInputs: number;
}

interface TestExtension extends ExtensionMixin<TestExtension, TestExtensionState>, TestExtensionState {
  deserializeInputs(this: TestExtension & Block, xmlElement: Element): void;

  deserializeInputs(this: TestExtension & Block, xmlElement: Element): void;

  getIndexForNewInput(this: TestExtension & Block, connection: Blockly.Connection): number | null;

  deserializeCounts(this: TestExtension & Block, xmlElement: Element): void;

  onPendingConnection(this: TestExtension & Block, connection: Blockly.Connection): void;

  finalizeConnections(this: TestExtension & Block): void;
}

export const TestExtensionName = "text_extension";

export const TestExtensionMixin: TestExtension = {
  /* eslint-enable @typescript-eslint/naming-convention */
  /** Counter for the next input to add to this block. */
  inputCounter: 2,

  /** Minimum number of inputs for this block. */
  minInputs: 0,

  /**
   * Create XML to represent number of text inputs.
   * @returns XML storage element.
   */
  mutationToDom(this) {
    const container = Blockly.utils.xml.createElement("mutation");
    const inputNames = this.inputList.map((input: Blockly.Input) => input.name).join(",");
    container.setAttribute("inputs", inputNames);
    container.setAttribute("next", String(this.inputCounter));
    return container;
  },

  /**
   * Parse XML to restore the text inputs.
   * @param xmlElement XML storage element.
   */
  domToMutation(this, xmlElement) {
    if (xmlElement.getAttribute("inputs")) {
      this.deserializeInputs(xmlElement);
    } else {
      this.deserializeCounts(xmlElement);
    }
  },
  saveExtraState() {
    return undefined;
  },
  loadExtraState(state) {},

  /**
   * Parses XML based on the 'inputs' attribute (non-standard).
   * @param xmlElement XML storage element.
   */
  deserializeInputs(this, xmlElement) {
    const items = xmlElement.getAttribute("inputs");
    if (items) {
      const inputNames = items.split(",");
      this.inputList = [];
      inputNames.forEach((name) => this.appendValueInput(name));
      // this.inputList[0].appendField(Blockly.Msg["LISTS_CREATE_WITH_INPUT_WITH"]);
    }
    const next = parseInt(xmlElement.getAttribute("next") ?? "0", 10) || 0;
    this.inputCounter = next;
  },

  /**
   * Parses XML based on the 'items' attribute (standard).
   * @param xmlElement XML storage element.
   */
  deserializeCounts(this, xmlElement) {
    const itemCount = Math.max(parseInt(xmlElement.getAttribute("items") ?? "0", 10), this.minInputs);
    // Two inputs are added automatically.
    for (let i = this.minInputs; i < itemCount; i++) {
      this.appendValueInput("ADD" + i);
    }
    this.inputCounter = itemCount;
  },

  /**
   * Check whether a new input should be added and determine where it should go.
   * @param connection The connection that has a pending connection.
   * @returns The index before which to insert a new input, or null if no input
   *     should be added.
   */
  getIndexForNewInput(this, connection) {
    if (!connection.targetConnection) {
      // this connection is available
      return null;
    }

    let connectionIndex = -1;
    for (let i = 0; i < this.inputList.length; i++) {
      if (this.inputList[i].connection == connection) {
        connectionIndex = i;
      }
    }

    if (connectionIndex == this.inputList.length - 1) {
      // this connection is the last one and already has a block in it, so
      // we should add a new connection at the end.
      return this.inputList.length + 1;
    }

    const nextInput = this.inputList[connectionIndex + 1];
    const nextConnection = nextInput?.connection?.targetConnection;
    if (nextConnection && !nextConnection.getSourceBlock().isInsertionMarker()) {
      return connectionIndex + 1;
    }

    // Don't add new connection
    return null;
  },

  /**
   * Called by a monkey-patched version of InsertionMarkerManager when
   * a block is dragged over one of the connections on this block.
   * @param connection The connection on this block that has a pending
   *     connection.
   */
  onPendingConnection(this, connection) {
    const insertIndex = this.getIndexForNewInput(connection);
    if (insertIndex == null) {
      return;
    }
    this.appendValueInput("ADD" + this.inputCounter++);
    this.moveNumberedInputBefore(this.inputList.length - 1, insertIndex);
  },

  /**
   * Called by a monkey-patched version of InsertionMarkerManager when a block
   * drag ends if the dragged block had a pending connection with this block.
   */
  finalizeConnections(this) {
    if (this.inputList.length > this.minInputs) {
      let toRemove: string[] = [];
      this.inputList.forEach((input: Blockly.Input) => {
        if (!input.connection?.targetConnection) {
          toRemove.push(input.name);
        }
      });

      if (this.inputList.length - toRemove.length < this.minInputs) {
        // Always show at least two inputs
        toRemove = toRemove.slice(this.minInputs);
      }
      toRemove.forEach((inputName) => this.removeInput(inputName));
      // The first input should have the block text. If we removed the
      // first input, add the block text to the new first input.
      if (this.inputList[0].fieldRow.length == 0) {
        this.inputList[0].appendField(Blockly.Msg["LISTS_CREATE_WITH_INPUT_WITH"]);
      }
    }
  },
};
