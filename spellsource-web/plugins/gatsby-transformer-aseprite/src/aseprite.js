const { createFileNodeFromBuffer } = require(`gatsby-source-filesystem`)
const pify = require('pify')
const fs = require('fs')
const Aseprite = require('./node-aseprite')
const sharp = require('sharp')
const _ = require(`lodash`)
const readFile = pify(fs.readFile)

class AsepriteDocument {
  constructor (path) {
    this.path = path
  }

  get width () {
    return this._aseFile.header.width
  }

  get height () {
    return this._aseFile.header.height
  }

  async parse () {
    if (!!this._aseFile) {
      return
    }

    this._aseFile = Aseprite.parse(await readFile(this.path), {
      clean: false,
      inflate: true
    })
  }

  async createDocumentNode ({
    createNodeId,
    createNode,
    createParentChildLink,
    createContentDigest,
    createFileNodeFromBuffer,
    cache,
    store,
    node
  }) {
    if (!this._aseFile) {
      throw new Error('parse first')
    }
    if (!!this._documentNode) {
      return this._documentNode
    }

    const header = {
      name: node.name,
      width: this._aseFile.header.width,
      height: this._aseFile.header.height
    }
    const documentNode = {
      id: createNodeId(`${node.absolutePath} parsed`),
      aseprite: header,
      internal: {
        contentDigest: createContentDigest(header),
        type: 'Aseprite'
      }
    }
    createNode(documentNode)
    createParentChildLink({
      parent: node,
      child: documentNode
    })
    this._documentNode = documentNode

    const fullRender = await this.toPng()
    const parentNodeId = documentNode.id
    const ext = '.png'
    const name = node.name
    const buffer = fullRender
    const fileNode = await createFileNodeFromBuffer({
      createNode,
      createNodeId,
      cache,
      store,
      buffer,
      name,
      ext,
      parentNodeId
    })
    createParentChildLink({
      parent: documentNode,
      child: fileNode
    })
    return documentNode
  }

  get _sharpArgs () {
    if (!this._aseFile) {
      throw new Error('parse first')
    }
    return {
      raw: {
        width: this._aseFile.header.width,
        height: this._aseFile.header.height,
        channels: 4,
        background: { r: 0, g: 0, b: 0, alpha: 0 }
      }
    }
  }

  async createSliceNodes ({
    createNodeId,
    createNode,
    createParentChildLink,
    createContentDigest,
    createFileNodeFromBuffer,
    cache,
    store,
    node
  }) {
    if (!this._aseFile) {
      throw new Error('parse first')
    }

    if (!!this._sliceNodes) {
      return this._sliceNodes
    }

    const documentNode = await this.createDocumentNode({
      createNodeId,
      createNode,
      createParentChildLink,
      createContentDigest,
      createFileNodeFromBuffer,
      cache,
      store,
      node
    })

    const slices = this.slices
    const nodes = []
    for (const slice of slices) {
      const asepriteData = {
        name: slice.data.name,
        numKeys: slice.data.numKeys,
        keys: slice.data.keys.map(key => {return {width: key.width, height: key.height, x: key.x, y: key.y}}),
        chunkType: 0x2022,
        chunkName: 'Slice'
      }

      const descendantNode = {
        id: createNodeId(`${node.absolutePath} ${slice.name}`),
        aseprite: asepriteData,
        children: [],
        parent: documentNode.id,
        internal: {
          contentDigest: createContentDigest(asepriteData),
          type: 'Aseprite'
        }
      }
      createNode(descendantNode)
      createParentChildLink({
        parent: documentNode,
        child: descendantNode
      })

      nodes.push(descendantNode)
      for (const key of slice.data.keys) {
        const keyBuffer = await this._sliceToPng({ key })
        const parentNodeId = descendantNode.id
        const ext = '.png'
        const name = slice.data.name
        const buffer = keyBuffer
        const fileNode = await createFileNodeFromBuffer({
          createNode,
          createNodeId,
          cache,
          store,
          buffer,
          name,
          ext,
          parentNodeId
        })
        createParentChildLink({
          parent: descendantNode,
          child: fileNode
        })
        nodes.push(fileNode)
      }
    }
    this._sliceNodes = nodes
    return nodes
  }

  get slices () {
    if (!!this._slices) {
      return this._slices
    }
    const sliceType = 0x2022
    this._slices = this._aseFile.frames[0].chunks.filter(chunk => chunk.type === sliceType)
    return this._slices
  }

  async toPng () {
    const buffer = await this.toBuffer()
    return await sharp(buffer, this._sharpArgs).png().toBuffer()
  }

  async toBuffer () {
    if (!!this._fullRender) {
      return this._fullRender
    }

    const bgPromise = sharp({
      create: {
        width: this._aseFile.header.width,
        height: this._aseFile.header.height,
        channels: 4,
        background: { r: 0, g: 0, b: 0, alpha: 0 }
      }
    }).png()
      .toBuffer()

    const celType = 8197
    // Get the cels for the first frame
    const cels = this._aseFile.frames[0].chunks.filter(chunk => chunk.type === celType)

    // Create png image buffers per cel to create an image of the first frame (creating the Promises to be used)
    const otherPromises = cels.map(cel => {
      return sharp(cel.data.pixels, {
        raw: {
          width: cel.data.width,
          height: cel.data.height,
          channels: this._aseFile.header.pixelFormat / 8
        }
      })
        .png()
        .toBuffer()
    })

    // Run the promises all at once to get the buffers for the base image and the cels to combine
    const [bg, ...others] = await Promise.all([bgPromise, ...otherPromises])
      .catch(console.error)

    const composite = sharp(bg)
      .composite(others.map((img, index) => ({
        input: img,
        top: cels[index].data.y,
        left: cels[index].data.x,
      })))

    this._fullRender = await composite.raw().toBuffer()
    return this._fullRender
  }

  async _sliceToPng ({ key }) {
    const buffer = await this.toBuffer()
    return await sharp(buffer, this._sharpArgs)
      .extract({
        left: key.x,
        top: key.y,
        width: key.width,
        height: key.height
      })
      .png()
      .toBuffer()
  }
}

module.exports.AsepriteDocument = AsepriteDocument