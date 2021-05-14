// gatsby-node.js
const { createFileNodeFromBuffer } = require(`gatsby-source-filesystem`)
const pify = require('pify')
const streamReadAll = require('stream-read-all')
const PSD = require(`psd.js`)
const _ = require(`lodash`)
const defaultPluginOptions = {
  createName: (node, psd, descendant) => {
    return `${descendant.name}`
  },
  shouldRender: (descendant) => {
    if (descendant.isGroup()) {
      return false
    }

    if (descendant.type !== 'layer' || !!descendant.text) {
      return false
    }

    if (!descendant.visible) {
      return false
    }

    return true
  }
}

exports.onCreateNode = async (args, pluginOptions) => {
  const { node, actions, createNodeId, cache, store, createContentDigest } = args
  const { createNode, createParentChildLink } = actions

  // find all the photoshop files so far
  if (node.internal.mediaType !== 'image/vnd.adobe.photoshop') {
    return
  }

  const psd = await pify(PSD.open(node.absolutePath))
  const tree = psd.tree()
  const exportedDocumentContent = {
    width: tree.width,
    height: tree.height,
  }
  // insert the root document node
  const documentNode = {
    id: createNodeId(`${node.absolutePath} document`),
    photoshop: { document: exportedDocumentContent, type: 'root' },
    children: [],
    parent: node.id,
    internal: {
      contentDigest: createContentDigest(exportedDocumentContent),
      type: 'Photoshop'
    }
  }
  createNode(documentNode)
  createParentChildLink({
    parent: node,
    child: documentNode
  })
  try {
    // render the whole photoshop document
    const png = psd.image.toPng()
    const parentNodeId = documentNode.id
    const ext = '.png'
    const name = node.name
    const buffer = await streamReadAll(png.pack())
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
  } catch (e) {}

  let i = 0
  for (const descendant of tree.descendants()) {
    const exportedDescendantContent = descendant.export()
    delete descendant.children

    const descendantNode = {
      id: createNodeId(`${node.absolutePath} ${i}`),
      photoshop: {
        type: descendant.type,
        ...exportedDescendantContent
      },
      children: [],
      parent: documentNode.id,
      internal: {
        contentDigest: createContentDigest(exportedDescendantContent),
        type: 'Photoshop'
      }
    }
    createNode(descendantNode)
    createParentChildLink({ parent: documentNode, child: descendantNode })

    if ((pluginOptions.shouldRender || defaultPluginOptions.shouldRender)(descendant)) {
      try {
        const png = descendant.toPng()
        const parentNodeId = descendantNode.id
        const ext = '.png'
        const name = (pluginOptions.createName || defaultPluginOptions.createName)(node, psd, descendant)
        const buffer = await streamReadAll(png.pack())
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
      } catch (err) {}
    }
  }
}