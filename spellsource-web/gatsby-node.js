const lodash = require(`lodash`)
const path = require(`path`)
const { resolveArt } = require('./src/lib/resolve-art.ts')
const { createFilePath } = require("gatsby-source-filesystem")

exports.onCreateWebpackConfig = ({
  stage,
  loaders,
  actions,
}) => {
  if (stage === 'build-html') {
    actions.setWebpackConfig({
      externals: [{
        canvas: 'canvas',
      }],
    })
  }
}

exports.createSchemaCustomization = ({ actions }) => {
  const { createTypes } = actions
  const typeDefs = `
  type ToolboxBlockCategoryList implements Node {
    BlockTypePrefix: String
    CategoryName: String
    ColorHex: String
    Subcategories: [String]
    Tooltip: String
    Subtooltips: [String]
    Custom: String
    ButtonName: String
    ButtonCallback: String
  }
  type Block implements Node {
    type: String
    messages: [String]
    output: String
    colour: String
    nextStatement: [String]
    previousStatement: [String]
    args: [Args1]
    data: String
    inputsInline: Boolean
    hat: String
    searchMessage: String
    comment: String
    subcategory: String
    plural: Boolean
  }
  type Args1 {
    i: Int!
    args: [Args]
  }
  type Args {
    type: String
    check: [String]
    name: String
    valueI: Int
    valueS: String
    valueB: Boolean
    min: Int
    max: Int
    int: Boolean
    text: String
    options: [[String]]
    shadow: Shadow
    width: Int
    height: Int
    alt: String
    src: String
    variable: String
    variableTypes: [String]
    defaultType: String
  }
  type Shadow {
    type: String
    fields: [Field]
    notActuallyShadow: Boolean
  }
  type Field {
    name: String
    valueI: Int
    valueS: String
  }
  `
  createTypes(typeDefs)
}

exports.createPages = async ({ actions, graphql, reporter }) => {
  const { createPage } = actions

  const pageTemplate = path.resolve(`src/templates/page-template.js`)
  const cardTemplate = path.resolve(`src/templates/card-template.js`)
  const wikiTemplate = path.resolve(`src/templates/wiki-template.js`)
  const collectionTemplate = path.resolve("./src/templates/collection-template.js")

  const result = await graphql(`
    {
      allMarkdownRemark(
        sort: { order: DESC, fields: [frontmatter___date] }
        limit: 1000
      ) {
        edges {
          node {
            frontmatter {
              path
              layout
            }
          }
        }
      }
      
      allCard {
        edges {
          node {
            id
          }
        }
      }
    }
  `)

  // Handle errors
  if (result.errors) {
    reporter.panicOnBuild(`Error while running GraphQL query.`)
    return
  }

  // Create pages from the markdown files
  result.data.allMarkdownRemark.edges.forEach(({ node }) => {
    // Is this a wiki page? then use the wiki template
    let template = node.frontmatter.layout === 'wiki' ? wikiTemplate : pageTemplate
    createPage({
      path: node.frontmatter.path,
      component: template,
      context: {}
    })
  })

  // Create pages for each Card JSON
  result.data.allCard.edges.forEach(({ node }) => {
    createPage({
      path: '/cards/' + node.id,
      component: cardTemplate,
      context: {}
    })
  })

  // pagination for collection
  const cardsInCollection = result.data.allCard.edges
  const cardsPerPage = 12
  const numPages = Math.ceil(cardsInCollection.length / cardsPerPage)
  Array.from({ length: numPages }).forEach((_, i) => {
    createPage({
      path: i === 0 ? `/collection` : `/collection/${i + 1}`,
      component: collectionTemplate,
      context: {
        limit: cardsPerPage,
        skip: i * cardsPerPage,
        numPages,
        currentPage: i + 1,
      },
    })
  })
}

exports.createResolvers = ({ createResolvers }) => {
  const resolvers = {
    Card: {
      art: {
        resolve (source, args, context, info) {
          return resolveArt(source, context)
        }
      }
    },
    Block: {
      searchMessage: {
        resolve (source, args, context, info) {
          let node = source
          const getNodeForBlockType = (type) => {
            return context.nodeModel.getNodeById({
              id: type
            })
          }
          const getTextForNode = (node) => {
            let text = ''
            for (let i = 0; i < node.messages.length; i++) {
              let message = node.messages[i]
              if (!!node.args && !!node.args[i] && !!node.args[i].args) {
                let args = node.args[i].args
                for (let j = 0; j < args.length; j++) {
                  let text = getTextForArg(args[j])
                  message = message.replace('%' + (j + 1).toString(), text)
                }
              }
              text += message + ' '
            }
            return text
          }
          const getTextForArg = (arg) => {
            if (!!arg.shadow) {
              let shadowType = arg.shadow.type
              let shadowNode = getNodeForBlockType(shadowType)
              return getTextForNode(shadowNode)
            }
            if (!!arg.options) {
              let text = ''
              for (let option of arg.options) {
                text += option[0] + ' '
              }
              return text
            }
            if (arg.type === 'field_label_plural') {
              return arg.value
            }
            return ''
          }
          return getTextForNode(node).replace(/\s+/g, ' ').trim()
          //removing excess whitespace just in case ^^^
        }
      }
    }
  }
  createResolvers(resolvers)
}