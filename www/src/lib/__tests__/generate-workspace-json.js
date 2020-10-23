import WorkspaceUtils from '../workspace-utils'
import JsonConversionUtils from '../json-conversion-utils'
import fs from 'fs'
import path from 'path'
import java from 'java'
import { Workspace } from 'blockly'
import { beforeAll, describe, expect, test, afterAll } from '@jest/globals'
import { jsonTransformFileNode } from '../json-transforms'
import BlocklyMiscUtils from '../blockly-misc-utils'
import { walk, walkSync } from '../walk'
import SpellsourceTesting from '../spellsource-testing'

const cardsPath = `${__dirname}/../../../../cards/src/main/resources/cards/collectible`
const cardsPath2 = `${__dirname}/../../../../game/src/main/resources/basecards/standard`
const blocksPath = `${__dirname}/../../../../unityclient/src/unity/Assets/UBlockly/JsonBlocks/`

const cards = []
for (const f of walkSync(cardsPath)) {
  const strings = f.split('/')
  const id = strings[strings.length - 1].replace(/.json$/, '')
  cards.push([id, f])
}

const usedBlocks = {}

const weirdos = []

java.classpath.push(`${__dirname}/../../../../www/build/libs/www-0.8.89-all.jar`)

describe('WorkspaceUtils', () => {
  beforeAll(async () => {
    const blockEdges = []
    const cardEdges = []
    const jsonEdges = []
    const data = {
      allBlock: { edges: blockEdges },
      allCard: { edges: cardEdges },
      allJSON: { edges: jsonEdges }
    }

    for await (const blocksDefPath of walk(blocksPath)) {
      if (!blocksDefPath.endsWith('.json')) {
        continue
      }

      const blocksJson = JSON.parse(await fs.promises.readFile(blocksDefPath))
      for (const blockJson of blocksJson) {
        jsonTransformFileNode(blockJson, { base: path.basename(blocksDefPath) })
        blockEdges.push({ node: blockJson })
      }
    }

    const handleWalk = async (cardPath) => {
      if (!cardPath.endsWith('.json')) {
        return
      }
      const file = await fs.promises.readFile(cardPath)
      const cardJson = JSON.parse(file)
      jsonTransformFileNode(cardJson, { base: path.basename(cardPath) })
      cardEdges.push({ node: cardJson })
      jsonEdges.push({ node: { internal: { content: file } } })
    }

    for await(const cardPath of walk(cardsPath)) {
      await handleWalk(cardPath)
    }
    for await(const cardPath of walk(cardsPath2)) {
      await handleWalk(cardPath)
    }

    BlocklyMiscUtils.initBlocks(data)
    BlocklyMiscUtils.initHeroClassColors(data)
    BlocklyMiscUtils.initCardBlocks(data)
  })

  test.each(cards)('generates card %s ', async (id, cardPath) => {
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.generateCard(workspace, srcCard)
    WorkspaceUtils.workspaceToCardScript(workspace)
  })

  test.each(cards)('no custom generates card %s', async (id, cardPath) => {
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.errorOnCustom = true
    JsonConversionUtils.generateCard(workspace, srcCard)
    WorkspaceUtils.workspaceToCardScript(workspace)
  })

  test.each(cards)('deep equals card %s ', async (id, cardPath) => {
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.generateCard(workspace, srcCard)
    const json = WorkspaceUtils.workspaceToCardScript(workspace)
    expect(json).toEqual(srcCard)
  })

  test.each(cards)('replays the same %s', async (id, cardPath) => {
    const ConversionHarness = java.import('com.hiddenswitch.spellsource.conversiontest.ConversionHarness')
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.generateCard(workspace, srcCard)
    const json = WorkspaceUtils.workspaceToCardScript(workspace)
    const result = ConversionHarness.assertCardReplaysTheSameSync(1, 2, id, JSON.stringify(json))
    if (!result || result == 'false') {
      expect(json).toEqual(srcCard)
      weirdos.push(id)
    } else {
      expect(result).toEqual(true)
    }
  })

  test.each(cards)('no custom and replays the same %s', async (id, cardPath) => {
    const ConversionHarness = java.import('com.hiddenswitch.spellsource.conversiontest.ConversionHarness')
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.errorOnCustom = true
    JsonConversionUtils.generateCard(workspace, srcCard)
    const json = WorkspaceUtils.workspaceToCardScript(workspace)
    expect(ConversionHarness.assertCardReplaysTheSameSync(1, 2, id, JSON.stringify(json))).toEqual(true)
  })

  test('java test', async () => {
    const context = SpellsourceTesting.runGym()
    var minion1 = SpellsourceTesting.playMinion(context, 'PLAYER_1', 'minion_dead_horse')
    var card1 = SpellsourceTesting.receiveCard(context, 'PLAYER_1', 'minion_tiny_persecutor')
    var minion2 = SpellsourceTesting.playMinion(context, 'PLAYER_1', card1, minion1)

    console.log(context.getPlayer1Sync().getMinionsSync().getSync(0).getHpSync())
  })

  test('just dreams of strength', async () => {
    const ConversionHarness = java.import('com.hiddenswitch.spellsource.conversiontest.ConversionHarness')
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
    `
    expect(ConversionHarness.assertCardReplaysTheSameSync(1, 2, 'spell_dreams_of_strength', json)).toEqual(true)
  })

  test.each(cards)('bug test time %s', async (id, cardPath) => {
    const ConversionHarness = java.import('com.hiddenswitch.spellsource.conversiontest.ConversionHarness')
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.generateCard(workspace, srcCard)
    const json = WorkspaceUtils.workspaceToCardScript(workspace)
    ConversionHarness.assertCardReplaysTheSameSync(1, 2, id, JSON.stringify(json))

    const ConversionHarness2 = java.import('com.hiddenswitch.spellsource.conversiontest.ConversionHarness')
    const workspace2 = new Workspace()
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
    `)
    JsonConversionUtils.generateCard(workspace2, srcCard2)
    const json2 = WorkspaceUtils.workspaceToCardScript(workspace)
    expect(ConversionHarness.assertCardReplaysTheSameSync(1, 2, id, JSON.stringify(json))).toEqual(true)

  })

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
})