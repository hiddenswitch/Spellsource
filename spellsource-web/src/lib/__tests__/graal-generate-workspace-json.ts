import * as WorkspaceUtils from "../workspace-utils";
import * as JsonConversionUtils from "../json-conversion-utils";
import { Workspace } from "blockly";
import { beforeAll, describe, expect, test } from "@jest/globals";
import * as BlocklyMiscUtils from "../blockly-misc-utils";
import SpellsourceTesting from "../spellsource-testing";
import { getAllBlockJson, getAllIcons, readAllJsonSync } from "../fs-utils";
import { keyBy } from "lodash";
import { BlocklyDataContext } from "../../pages/card-editor";
import { ContextType } from "react";
import path from "path";
import { transformCard } from "../json-transforms";

const cardsPath = `../spellsource-cards-git/src/main/resources/cards/collectible`;
const cardsPath2 = `../spellsource-game/src/main/resources/basecards`;
const weirdos = [];

const cards = [
  ...readAllJsonSync(path.join(cardsPath, "**", "*.json"), transformCard),
  ...readAllJsonSync(path.join(cardsPath2, "**", "*.json"), transformCard),
].map((card) => [card.id, card] as const);

declare var Java: {
  addToClasspath: (arg0: string) => void;
  type: (arg0: string) => any;
};
if (typeof Java !== "undefined") {
  // from graal
  const ClasspathCardCatalogue: {
    INSTANCE: {
      loadCardsFromPackage: () => void;
      getCards: () => Map<string, any> & {
        size: () => number;
      };
    };
  } = Java.type("net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue");

  describe("WorkspaceUtils", () => {
    beforeAll(async () => {
      const allBlocks = await getAllBlockJson();
      const blocksByType = keyBy(allBlocks, (block) => block.type);
      const allIcons = await getAllIcons();

      const data: ContextType<typeof BlocklyDataContext> = {
        allBlocks,
        blocksByType,
        allIcons,
        userId: "",
        myCards: [],
        allArt: [],
        classes: Object.fromEntries(cards.filter(([, card]) => card.type === "CLASS")),
      };
      BlocklyMiscUtils.initBlocks(data);
      BlocklyMiscUtils.initHeroClassColors(data);
      // BlocklyMiscUtils.initCardBlocks(data);
    });

    test.each(cards)("generates %s ", async (id, srcCard) => {
      const workspace = new Workspace();
      JsonConversionUtils.generateCard(workspace, srcCard);
      WorkspaceUtils.workspaceToCardScript(workspace);
      expect(workspace.getTopBlocks(false).length).toBeGreaterThan(0);
    });

    test.each(cards)("no custom generates card %s", async (id, srcCard) => {
      const workspace = new Workspace();
      JsonConversionUtils.setErrorOnCustom(true);
      JsonConversionUtils.generateCard(workspace, srcCard);
      WorkspaceUtils.workspaceToCardScript(workspace);
      // emit something so that the test registers as in progress
      expect(true).toEqual(true);
    });

    test.each(cards)("deep equals card %s ", async (id, srcCard) => {
      const workspace = new Workspace();
      JsonConversionUtils.generateCard(workspace, srcCard);
      const json = WorkspaceUtils.workspaceToCardScript(workspace);
      expect(json).toEqual(srcCard);
    });

    test.each(cards)("replays the same %s", async (id, srcCard) => {
      const ConversionHarness = Java.type("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
      const workspace = new Workspace();
      JsonConversionUtils.generateCard(workspace, srcCard);
      const json = WorkspaceUtils.workspaceToCardScript(workspace);
      const result: boolean | string | null = ConversionHarness.assertCardReplaysTheSame(
        1,
        2,
        srcCard.id,
        JSON.stringify(json)
      );
      if (!result || result == "false") {
        expect(json).toEqual(srcCard);
        weirdos.push(srcCard.id);
      } else {
        expect(result).toEqual(true);
      }
    });

    test.each(cards)("no custom and replays the same %s", async (id, srcCard) => {
      const ConversionHarness = Java.type("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
      const workspace = new Workspace();
      JsonConversionUtils.setErrorOnCustom(true);
      JsonConversionUtils.generateCard(workspace, srcCard);
      const json = WorkspaceUtils.workspaceToCardScript(workspace);
      expect(ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json))).toEqual(true);
    });

    test("java test", async () => {
      const context = SpellsourceTesting.runGym();
      const minion1 = SpellsourceTesting.playMinion(context, "PLAYER_1", "minion_dead_horse");
      const card1 = SpellsourceTesting.receiveCard(context, "PLAYER_1", "minion_tiny_persecutor");
      const minion2 = SpellsourceTesting.playMinion(context, "PLAYER_1", card1, minion1);

      expect(context.getPlayer1().getMinions().get(0).getHp()).toEqual(6);
    });

    test("just dreams of strength", async () => {
      const ConversionHarness = Java.type("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
      const json = `
    {
      "name": "Dreams of Strength",
      "baseManaCost": 7,
      "type": "SPELL",
      "heroClass": "AMBER",
      "rarity": "EPIC",
      "description": "Give a Larva +7/+7 and Guard.",
      "targetSelection": "FRIENDLY_MINIONS",
      "spell": {
        "class": "MetaSpell",
        "spells": [
          {
            "class": "BuffSpell",
            "value": 7
          },
          {
            "class": "AddAttributeSpell",
            "attribute": "TAUNT"
          }
        ],
        "filter": {
          "class": "SpecificCardFilter",
          "card": "token_spiderling"
        }
      },
      "collectible": true,
      "set": "VERDANT_DREAMS",
      "fileFormatVersion": 1
    }
    `;
      expect(ConversionHarness.assertCardReplaysTheSame(1, 2, "spell_dreams_of_strength", json)).toEqual(true);
    });

    test.each(cards)("bug test time %s", async (id, srcCard) => {
      const ConversionHarness = Java.type("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
      const workspace = new Workspace();
      JsonConversionUtils.generateCard(workspace, srcCard);
      const json = WorkspaceUtils.workspaceToCardScript(workspace);
      ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json));

      const ConversionHarness2 = Java.type("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
      const workspace2 = new Workspace();
      const srcCard2 = JSON.parse(`
      {
      "name": "Dreams of Strength",
      "baseManaCost": 7,
      "type": "SPELL",
      "heroClass": "AMBER",
      "rarity": "EPIC",
      "description": "Give a Larva +7/+7 and Guard.",
      "targetSelection": "FRIENDLY_MINIONS",
      "spell": {
        "class": "MetaSpell",
        "spells": [
          {
            "class": "BuffSpell",
            "value": 7
          },
          {
            "class": "AddAttributeSpell",
            "attribute": "TAUNT"
          }
        ],
        "filter": {
          "class": "SpecificCardFilter",
          "card": "token_spiderling"
        }
      },
      "collectible": true,
      "set": "VERDANT_DREAMS",
      "fileFormatVersion": 1
    }
    `);
      JsonConversionUtils.generateCard(workspace2, srcCard2);
      const json2 = WorkspaceUtils.workspaceToCardScript(workspace);
      expect(ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json))).toEqual(true);
    });
  });
}
