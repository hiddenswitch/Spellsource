const path = require('path')
const publicPath = './public'
const fs = require('fs-extra')
const pify = require('pify')
const ensureDir = pify(fs.ensureDir)
const writeFile = pify(fs.writeFile)
const { markdown } = require('./src/markdown')

async function createPages ({ graphql }) {
  const result = await graphql(`
    {
      allMarkdownRemark {
        edges {
          node {
            rawMarkdownBody
            frontmatter {
              path
            }
          }
        }
      }
    }
  `)

  for (let { node } of result.data.allMarkdownRemark.edges) {
    const text = markdown(node.rawMarkdownBody)
    const ensureDirs = path.join(publicPath, `${node.frontmatter.path}`)
    const destinationPath = path.join(ensureDirs, `index.txt`)

    await ensureDir(ensureDirs)
    await writeFile(
      destinationPath,
      text,
      'utf8'
    ).catch(r => {
      console.error(`Failed to write ${destinationPath}`, r)
    })
  }
}

exports.createPages = createPages