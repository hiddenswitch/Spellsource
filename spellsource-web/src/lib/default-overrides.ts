import { getBlockInfo } from "./blockly-utils";

export function overrideAll() {
  math();
  loops();
  text();
  logic();
}

export function numShadow(type, name, value = null) {
  addShadow(type, name, "math_number", "NUM", value);
}

export function textShadow(type, name, value = null) {
  addShadow(type, name, "text", "TEXT", value);
}

export function addShadow(type, name, shadowType, fieldName = null, value = null) {
  const toolboxInfo = getBlockInfo(type);

  toolboxInfo.inputs[name] = {
    shadow: {
      type: shadowType,
      fields: fieldName
        ? {
            [fieldName]: value,
          }
        : {},
    },
  };
}

export function math() {
  numShadow("math_arithmetic", "A", 1);
  numShadow("math_arithmetic", "B", 1);

  numShadow("math_single", "NUM", 9);

  numShadow("math_trig", "NUM", 45);

  numShadow("math_number_property", "NUMBER_TO_CHECK");

  numShadow("math_round", "NUM", 3.1);

  numShadow("math_modulo", "DIVIDEND", 64);
  numShadow("math_modulo", "DIVISOR", 10);

  numShadow("math_constrain", "VALUE", 50);
  numShadow("math_constrain", "LOW", 1);
  numShadow("math_constrain", "HIGH", 100);

  numShadow("math_random_int", "FROM", 1);
  numShadow("math_random_int", "TO", 100);

  numShadow("math_atan2", "X", 3);
  numShadow("math_atan2", "Y", 4);
}

export function loops() {
  numShadow("controls_for", "FROM", 1);
  numShadow("controls_for", "TO", 10);
  numShadow("controls_for", "BY", 1);

  addShadow("controls_whileUntil", "BOOL", "logic_boolean");
}

export function text() {
  textShadow("text_append", "TEXT");

  textShadow("text_length", "VALUE", "abc");

  textShadow("text_isEmpty", "VALUE");

  textShadow("text_indexOf", "FIND", "abc");

  textShadow("text_changeCase", "TEXT", "abc");

  textShadow("text_trim", "TEXT", "abc");

  textShadow("text_print", "TEXT", "abc");

  textShadow("text_count", "TEXT", "abc");
  textShadow("text_count", "SUB", "a");

  textShadow("text_replace", "TEXT", "abc");
  textShadow("text_replace", "FROM", "abc");
  textShadow("text_replace", "TO", "cba");

  textShadow("text_reverse", "TEXT", "abc");
}

export function logic() {
  addShadow("controls_if", "IF0", "logic_boolean");
  addShadow("controls_ifelse", "IF0", "logic_boolean");

  addShadow("logic_negate", "BOOL", "logic_boolean");

  numShadow("logic_compare", "A", 1);
  numShadow("logic_compare", "B", 1);

  addShadow("logic_operation", "A", "logic_boolean");
  addShadow("logic_operation", "B", "logic_boolean");

  addShadow("logic_ternary", "IF", "logic_boolean");
}
