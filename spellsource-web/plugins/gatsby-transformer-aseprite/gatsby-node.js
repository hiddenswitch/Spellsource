// gatsby-node.js
const { createFileNodeFromBuffer } = require('gatsby-source-filesystem')
const { AsepriteDocument } = require('./src/aseprite')

exports.onCreateNode = async (args) => {
  const { node, actions, createNodeId, cache, store, createContentDigest } = args
  const { createNode, createParentChildLink } = actions

  // find all the aseprite files so far
  if (node.extension !== 'ase' && node.extension !== 'aseprite') {
    return
  }

  const aseprite = new AsepriteDocument(node.absolutePath)
  await aseprite.parse()
  await aseprite.createSliceNodes({
    node,
    actions,
    createNodeId,
    cache,
    store,
    createContentDigest,
    createNode,
    createParentChildLink,
    createFileNodeFromBuffer
  })
}