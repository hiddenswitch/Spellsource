import WorkspaceUtils from '../workspace-utils'
import JsonConversionUtils from '../json-conversion-utils'
import fs from 'fs'
import path from 'path'
import { Workspace } from 'blockly'
import { beforeAll, describe, expect, test } from '@jest/globals'
import { jsonTransformFileNode } from '../json-transforms'
import BlocklyMiscUtils from '../blockly-misc-utils'
import { walk, walkSync } from '../walk'
import java from 'java'

const cardsPath = `${__dirname}/../../../../cards/src/main/resources/cards/collectible`
const blocksPath = `${__dirname}/../../../../unityclient/Assets/UBlockly/JsonBlocks/`

const cards = []
for (const f of walkSync(cardsPath)) {
  const strings = f.split('/')
  const id = strings[strings.length - 1].replace(/.json$/, '')
  cards.push([id, f])
}

java.classpath.push('./build/libs/www-0.8.79-all.jar')
const ConversionHarness = java.import('com.hiddenswitch.spellsource.conversiontest.ConversionHarness')

describe('WorkspaceUtils', () => {
  beforeAll(async () => {
    const blockEdges = []
    const cardEdges = []
    const data = {
      allBlock: { edges: blockEdges },
      allCard: { edges: cardEdges }
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

    for await(const cardPath of walk(cardsPath)) {
      if (!cardPath.endsWith('.json')) {
        continue
      }

      const cardJson = JSON.parse(await fs.promises.readFile(cardPath))
      jsonTransformFileNode(cardJson, { base: path.basename(cardPath) })
      cardEdges.push({ node: cardJson })
    }

    BlocklyMiscUtils.initializeBlocks(data)
  })

  test.each(cards)('generates card %s ', async (id, cardPath) => {
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
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
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.generateCard(workspace, srcCard)
    const json = WorkspaceUtils.workspaceToCardScript(workspace)
    expect(ConversionHarness.assertCardReplaysTheSameSync(1, 2, id, JSON.stringify(json))).toEqual(true)
  })
})