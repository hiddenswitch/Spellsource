// ******************************************************************************
// See issue https://github.com/google/blockly/issues/4369
// Blockly will not allow you to avoid printing out a bunch of useless
// localization warnings for messages we don't use.
// This clogs up unit testing logs, etc.
// The versions for both log and warn are because they keep changing which ones they are
// using across builds.
const tempConsoleLog = console.log;
const tempConsoleWarn = console.warn;
console.log = (message) => {
  if (message.startsWith("WARNING: No message string for %{")) return;
  else tempConsoleLog(message);
};
console.warn = (message) => {
  if (message.startsWith("No message string for %{")) return;
  else tempConsoleWarn(message);
};
import * as WorkspaceUtils from "../workspace-utils";
import * as JsonConversionUtils from "../json-conversion-utils";
import { Workspace } from "blockly";
import { beforeAll, describe, expect, test } from "@jest/globals";
import * as BlocklyMiscUtils from "../blockly-misc-utils";
import SpellsourceTesting from "../spellsource-testing";
import java from "java";
import { getAllBlockJson, getAllIcons, readAllJsonSync } from "../fs-utils";
import { keyBy } from "lodash";
import { BlocklyDataContext } from "../../pages/card-editor";
import { ContextType } from "react";
import path from "path";
import { transformCard } from "../json-transforms";
import { promisify } from "util";

console.log = tempConsoleLog;
console.warn = tempConsoleWarn;

const cardsPath = `../spellsource-cards-git/src/main/resources/cards/collectible`;
const cardsPath2 = `../spellsource-game/src/main/resources/basecards`;

const usedBlocks = {};

const weirdos = [];

// requires graal
java.classpath.push(
  path.join(
    process.cwd(),
    "../spellsource-web-cardeditor-support/build/libs/spellsource-web-cardeditor-support-all.jar"
  )
);

java.options.push("--enable-preview");

// @ts-ignore
java.asyncOptions = {
  syncSuffix: "",
  asyncSuffix: "Callback",
  promiseSuffix: "Async",
  promisify,
};

const cards = [
  ...readAllJsonSync(path.join(cardsPath, "**", "*.json"), transformCard),
  ...readAllJsonSync(path.join(cardsPath2, "**", "*.json"), transformCard),
].map((card) => [card.id, card] as const);

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
    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
    const workspace = new Workspace();
    JsonConversionUtils.generateCard(workspace, srcCard);
    const json = WorkspaceUtils.workspaceToCardScript(workspace);
    try {
      const result = ConversionHarness.assertCardReplaysTheSame(1, 2, srcCard.id, JSON.stringify(json));
      if (result) {
        expect(result).toEqual(true);
        return;
      }
    } catch (e) {
      console.warn(e);
    }
    expect(json).toEqual(srcCard);
    weirdos.push(srcCard.id);
  });

  test.each(cards)("no custom and replays the same %s", async (id, srcCard) => {
    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
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

  test("just one card", async () => {
    const srcCard = Object.fromEntries(cards)["weapon_spirit_saber"];

    const ConversionHarness = java.import("com.hiddenswitch.spellsource.conversiontest.ConversionHarness");
    const workspace = new Workspace();
    JsonConversionUtils.generateCard(workspace, srcCard);
    const json = WorkspaceUtils.workspaceToCardScript(workspace);
    try {
      const result = ConversionHarness.assertCardReplaysTheSame(100, 200, srcCard.id, JSON.stringify(json));
      if (result) {
        expect(result).toEqual(true);
        return;
      }
    } catch (e) {
      console.warn(e);
    }
    expect(json).toEqual(srcCard);
    weirdos.push(srcCard.id);
  });

  test.each(cards)("bug test time %s", async (id, srcCard) => {
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
