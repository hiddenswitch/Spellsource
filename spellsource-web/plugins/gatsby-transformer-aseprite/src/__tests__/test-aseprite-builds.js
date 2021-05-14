const { AsepriteDocument } = require('../aseprite')
const sharp = require('sharp')
const pixelmatch = require('pixelmatch')

describe('aseprite parsing tests', () => {
  it('renders the whole document correctly', async () => {
    const testSprite = new AsepriteDocument(`${__dirname}/test.aseprite`)
    await testSprite.parse()
    const actual = await testSprite.toBuffer()
    const expected = await sharp(`${__dirname}/full.png`)
      .raw()
      .toBuffer()
    // pixelmatch returns number of different pixels
    expect(pixelmatch(actual, expected, null, testSprite.width, testSprite.height, { threshold: 0.001 })).toEqual(0)
  })

  it('gets slices', async () => {
    const testSprite = new AsepriteDocument(`${__dirname}/test.aseprite`)
    await testSprite.parse()

    const actual = testSprite.slices
    expect(actual.length).toEqual(21)
  })

  it('renders slices', async () => {
    const testSprite = new AsepriteDocument(`${__dirname}/test.aseprite`)
    await testSprite.parse()

    for (const slice of testSprite.slices) {
      const slicePng = await testSprite._sliceToPng({ key: slice.data.keys[0] })
      const actual = await sharp(slicePng).raw().toBuffer()
      const expected = await sharp(`${__dirname}/sliced/${slice.data.name}.png`).raw().toBuffer()
      expect(pixelmatch(actual, expected, null, slice.data.keys[0].width, slice.data.keys[0].height, { threshold: 0.001 })).toEqual(0)
    }
  })
})