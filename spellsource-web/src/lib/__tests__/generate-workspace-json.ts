import * as WorkspaceUtils from "../workspace-utils";
import * as JsonConversionUtils from "../json-conversion-utils";
import { Workspace } from "blockly";
import { beforeAll, describe, expect, test } from "@jest/globals";
import * as BlocklyMiscUtils from "../blockly-misc-utils";
import SpellsourceTesting from "../spellsource-testing";
import * as java from "java";
import { getAllBlockJson, getAllIcons, readAllJson } from "../fs-utils";
import { keyBy } from "lodash";
import { BlocklyDataContext } from "../../pages/card-editor";
import { ContextType } from "react";
import path from "path";
import { CardDef } from "../../components/card-display";
import { transformCard } from "../json-transforms";

const cardsPath = `${__dirname}/../../../../spellsource-cards-git/src/main/resources/cards/collectible`;
const cardsPath2 = `${__dirname}/../../../../spellsource-game/src/main/resources/basecards/standard`;

const cards: CardDef[] = [];

const usedBlocks = {};

const weirdos = [];

// requires graal
java.classpath.push(
  `${__dirname}/../../../../spellsource-web-cardeditor-support/build/libs/spellsource-web-cardeditor-support-0.9.0-all.jar`
);

describe("WorkspaceUtils", () => {
  beforeAll(async () => {
    const cards1 = await readAllJson(path.join(cardsPath, "**", "*.json"), transformCard);
    const cards2 = await readAllJson(path.join(cardsPath2, "**", "*.json"), transformCard);

    cards.push(...cards1);
    cards.push(...cards2);

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
      classes: Object.fromEntries(cards.filter((card) => card.type === "CLASS").map((card) => [card.id, card])),
    };
    BlocklyMiscUtils.initBlocks(data);
    BlocklyMiscUtils.initHeroClassColors(data);
    // BlocklyMiscUtils.initCardBlocks(data);
  });

  test.each(cards)("generates card %s ", async (srcCard) => {
    const workspace = new Workspace();
    JsonConversionUtils.generateCard(workspace, srcCard);
    WorkspaceUtils.workspaceToCardScript(workspace);
    // emit something so that the test registers as in progress
    expect(true).toEqual(true);
  });

  test.each(cards)("no custom generates card %s", async (srcCard) => {
    const workspace = new Workspace();
    JsonConversionUtils.setErrorOnCustom(true);
    JsonConversionUtils.generateCard(workspace, srcCard);
    WorkspaceUtils.workspaceToCardScript(workspace);
    // emit something so that the test registers as in progress
    expect(true).toEqual(true);
  });

  test.each(cards)("deep equals card %s ", async (srcCard) => {
    const workspace = new Workspace();
    JsonConversionUtils.generateCard(workspace, srcCard);
    const json = WorkspaceUtils.workspaceToCardScript(workspace);
    expect(json).toEqual(srcCard);
  });

  test.each(cards)("replays the same %s", async (srcCard) => {
    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
    const workspace = new Workspace();
    JsonConversionUtils.generateCard(workspace, srcCard);
    const json = WorkspaceUtils.workspaceToCardScript(workspace);
    const result = ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json));
    if (!result || result == "false") {
      expect(json).toEqual(srcCard);
      weirdos.push(srcCard.id);
    } else {
      expect(result).toEqual(true);
    }
  });

  test.each(cards)("no custom and replays the same %s", async (srcCard) => {
    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
    const workspace = new Workspace();
    JsonConversionUtils.setErrorOnCustom(true);
    JsonConversionUtils.generateCard(workspace, srcCard);
    const json = WorkspaceUtils.workspaceToCardScript(workspace);
    expect(ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json))).toEqual(true);
  });

  test("java test", async () => {
    const context = SpellsourceTesting.runGym();
    var minion1 = SpellsourceTesting.playMinion(context, "PLAYER_1", "minion_dead_horse");
    var card1 = SpellsourceTesting.receiveCard(context, "PLAYER_1", "minion_tiny_persecutor");
    var minion2 = SpellsourceTesting.playMinion(context, "PLAYER_1", card1, minion1);

    console.log(context.getPlayer1().getMinions().get(0).getHp());
  });

  test("just dreams of strength", async () => {
    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
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

  test.each(cards)("bug test time %s", async (srcCard) => {
    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
    const workspace = new Workspace();
    JsonConversionUtils.generateCard(workspace, srcCard);
    const json = WorkspaceUtils.workspaceToCardScript(workspace);
    ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json));

    const ConversionHarness2 = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
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

  /*
  afterAll(async () => {

    let blocks = JsonConversionUtils.customBlocks
    let keyArray = Object.keys(blocks)
      .sort((a, b) => blocks[b] - blocks[a])

    for (let i = 0; i < 5; i++) {
      let key = keyArray[i]
      let count = blocks[key]
      console.log('Appearing ' + count + ' times is ' + key)
    }



    for (let weirdo of weirdos) {
      console.log('Weirdo: ' + weirdo)
    }

  }, 1)

   */
});
