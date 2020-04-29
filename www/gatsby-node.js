const path = require(`path`)

exports.createPages = async ({ actions, graphql, reporter }) => {
  const { createPage } = actions

  const pageTemplate = path.resolve(`src/templates/page-template.js`)
  const wikiTemplate = path.resolve(`src/templates/wiki-template.js`)

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
    }
  `)

  // Handle errors
  if (result.errors) {
    reporter.panicOnBuild(`Error while running GraphQL query.`)
    return
  }

  result.data.allMarkdownRemark.edges.forEach(({ node }) => {
    // Is this a wiki page? then use the wiki template
    let template = node.frontmatter.layout === 'wiki' ? wikiTemplate : pageTemplate
    createPage({
      path: node.frontmatter.path,
      component: template,
      context: {}
    })
  })
}