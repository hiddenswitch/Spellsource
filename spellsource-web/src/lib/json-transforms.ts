import { CardDef } from "../components/collection/card-display";
import deepmerge from "deepmerge";
import path from "path";
import { BlockDef } from "./blockly-types";

export const transformBlock = (object: BlockDef) => {
  if (!object.id && object.type) {
    object.id = object.type;
  }

  for (let i = 0; i <= 9; i++) {
    const args = object[`args${i}`];
    let message = object[`message${i}`];
    if (message && args) {
      for (let j = 0; j < args.length; j++) {
        let arg = args[j];
        let token = "%" + (1 + j).toString();
        if (arg.type === "field_hidden" && !message.includes(token)) {
          message = token + message;
        }
      }
      object[`message${i}`] = message;
    } else {
      break;
    }
  }

  object.path = "/blocks/" + object.id;

  return object;
};

export const transformCard = (object: CardDef, file: string) => {
  if (!object.id) {
    // Set the id
    object.id = path.basename(file, ".json");
  }

  return object;
};

export const fixArt = (classes: Record<string, CardDef>) => {
  for (const card of Object.values(classes)) {
    const classCard = classes["class_" + card.heroClass];
    if (classCard) {
      card.art = deepmerge(classCard.art, card.art);
    }
  }
};
