import WorkspaceUtils from '../workspace-utils'
import JsonConversionUtils from '../json-conversion-utils'
import fs from 'fs'
import path from 'path'
import { Workspace } from 'blockly'
import { describe, test } from '@jest/globals'
import React from 'react'

const cardsPath = `${__dirname}/../../../../cards/src/main/resources/cards/collectible`

function * walkSync (dir) {
  const openedDir = fs.opendirSync(dir)
  try {
    let d = openedDir.readSync()
    while (d != null) {
      const entry = path.join(dir, d.name)
      if (d.isDirectory()) {
        yield * walkSync(entry)
      } else if (d.isFile()) {
        yield entry
      }
      d = openedDir.readSync()
    }
  } finally {
    openedDir.closeSync()
  }
}

const cards = []
for (const f of walkSync(cardsPath)) {
  const strings = f.split('/')
  const id = strings[strings.length - 1]
  cards.push([id, f])
}

describe('WorkspaceUtils', () => {
  test.each(cards)('generates card %s ', async (id, cardPath) => {
    const workspace = new Workspace()
    const srcCard = JSON.parse(await fs.promises.readFile(cardPath))
    JsonConversionUtils.generateCard(workspace, srcCard)
    const xml = WorkspaceUtils.workspaceToCardScript(workspace)
  })
})