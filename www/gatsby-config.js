module.exports = {
  siteMetadata: {
    title: `Spellsource`,
  },
  plugins: [
    {
      resolve: `gatsby-source-filesystem`,
      options: {
        name: `src`,
        path: `${__dirname}/src/`,
      },
    },
    {
      resolve: `gatsby-transformer-json-hooks`,
      options: {
        onTransformObject: ({ fileNode, object }) => {
          if (object.hasOwnProperty('fileFormatVersion')) {
            // Set the id
            object.id = fileNode.base.replace(/.json$/, '')
          }
        },
        typeName: ({ node, object, isArray }) => {
          // This is card JSON
          if (object.hasOwnProperty('fileFormatVersion')) {
            return 'Card'
          }
          return 'Json'
        }
      }
    },
    {
      resolve: `gatsby-source-filesystem`,
      options: {
        path: `${__dirname}/../cards/src/main/resources/cards/custom`,
      },
    },
    `gatsby-plugin-sass`,
    `gatsby-plugin-sharp`,
    `gatsby-image`,
    `gatsby-transformer-sharp`,
    {
      resolve: `gatsby-transformer-remark`,
      options: {
        plugins: [
          `gatsby-remark-copy-linked-files`,
          {
            resolve: `gatsby-remark-images`,
            options: {
              // It's important to specify the maxWidth (in pixels) of
              // the content container as this plugin uses this as the
              // base for generating different widths of each image.
              maxWidth: 650,
              linkImagesToOriginal: false,
              backgroundColor: 'transparent',
              withWebp: true,
              disableBgImage: true,
              quality: 100,
            },
          },
        ],
      },
    },
    {
      resolve: `gatsby-plugin-s3`,
      options: {
        bucketName: 'www.playspellsource.com',
        protocol: 'https',
        hostname: 'www.playspellsource.com',
      },
    },
    {
      resolve: `@gatsby-contrib/gatsby-plugin-elasticlunr-search`,
      options: {
        // Fields to index
        fields: [`title`, `tags`, `rawMarkdownBody`],
        // How to resolve each field`s value for a supported node type
        resolvers: {
          // For any node of type MarkdownRemark, list how to resolve the fields` values
          MarkdownRemark: {
            title: node => node.frontmatter.title,
            tags: node => node.frontmatter.tags,
            path: node => node.frontmatter.path,
            rawMarkdownBody: node => node.rawMarkdownBody
          },
        },
        // Optional filter to limit indexed nodes
        filter: (node, getNode) =>
          node.frontmatter.tags !== 'exempt',
      },
    },
  ],
}