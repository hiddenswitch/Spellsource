"use strict";

const _ = require(`lodash`);

const path = require(`path`);

async function onCreateNode({
  node,
  actions,
  loadNodeContent,
  createNodeId,
  createContentDigest
}, pluginOptions) {
  function getType({
    node,
    object,
    isArray
  }) {
    if (pluginOptions && _.isFunction(pluginOptions.typeName)) {
      return pluginOptions.typeName({
        node,
        object,
        isArray
      });
    } else if (pluginOptions && _.isString(pluginOptions.typeName)) {
      return pluginOptions.typeName;
    } else if (node.internal.type !== `File`) {
      return _.upperFirst(_.camelCase(`${node.internal.type} Json`));
    } else if (isArray) {
      return _.upperFirst(_.camelCase(`${node.name} Json`));
    } else {
      return _.upperFirst(_.camelCase(`${path.basename(node.dir)} Json`));
    }
  }

  function transformObject(obj, id, type) {
    const jsonNode = { ...obj,
      id,
      children: [],
      parent: node.id,
      internal: {
        contentDigest: createContentDigest(obj),
        type
      }
    };
    createNode(jsonNode);
    createParentChildLink({
      parent: node,
      child: jsonNode
    });
  }

  const {
    createNode,
    createParentChildLink
  } = actions; // We only care about JSON content.

  if (node.internal.mediaType !== `application/json`) {
    return;
  }

  const content = await loadNodeContent(node);
  const parsedContent = JSON.parse(content);

  if (_.isArray(parsedContent)) {
    parsedContent.forEach((obj, i) => {
      if (pluginOptions && _.isFunction(pluginOptions.onTransformObject)) {
        pluginOptions.onTransformObject({
          fileNode: node,
          object: obj
        });
      }

      transformObject(obj, obj.id ? String(obj.id) : createNodeId(`${node.id} [${i}] >>> JSON`), getType({
        node,
        object: obj,
        isArray: true
      }));
    });
  } else if (_.isPlainObject(parsedContent)) {
    if (pluginOptions && _.isFunction(pluginOptions.onTransformObject)) {
      pluginOptions.onTransformObject({
        fileNode: node,
        object: parsedContent
      });
    }

    transformObject(parsedContent, parsedContent.id ? String(parsedContent.id) : createNodeId(`${node.id} >>> JSON`), getType({
      node,
      object: parsedContent,
      isArray: false
    }));
  }
}

exports.onCreateNode = onCreateNode;