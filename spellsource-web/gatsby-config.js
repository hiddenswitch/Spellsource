// polyfill for graal
global.WebAssembly = global.WebAssembly || require("./src/lib/graal-compatibility/wasm-polyfill");

const remark = require("remark");
const visit = require("unist-util-visit");
const { resolveArt } = require("./src/lib/resolve-art");
const { jsonTransformFileNode, jsonType } = require("./src/lib/json-transforms");

module.exports = {
  siteMetadata: {
    title: `Spellsource`,
  },
  plugins: [
    `gatsby-plugin-flow`,
    {
      resolve: `gatsby-source-filesystem`,
      options: {
        name: `src`,
        path: `${__dirname}/src/`,
        ignore: [`${__dirname}/src/main/`, `${__dirname}/src/test/`, `**/*\.java`],
      },
    },

    {
      resolve: `gatsby-source-filesystem`,
      options: {
        path: `${__dirname}/../spellsource-cards-git/src/main/resources/cards`,
      },
    },
    {
      resolve: `gatsby-source-filesystem`,
      options: {
        path: `${__dirname}/../spellsource-game/src/main/resources/basecards/standard`,
      },
    },
    `gatsby-transformer-photoshop`,
    `gatsby-transformer-aseprite`,
    {
      resolve: `gatsby-markdown-textmesh-renderer`,
    },
    {
      resolve: `gatsby-transformer-json-hooks`,
      options: {
        onTransformObject: ({ fileNode, object }) => {
          jsonTransformFileNode(object, fileNode);
        },
        typeName: ({ node, object, isArray }) => {
          // This is card JSON
          return jsonType(object);
        },
      },
    },
    `gatsby-plugin-image`,
    `gatsby-plugin-sharp`,
    `gatsby-transformer-sharp`,
    `gatsby-plugin-sass`,
    `gatsby-plugin-anchor-links`,
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
              maxWidth: 700,
              linkImagesToOriginal: false,
              backgroundColor: "transparent",
              withWebp: true,
              disableBgImage: true,
              quality: 100,
              wrapperStyle: `float: right; width: 100%; margin-left: 0.5em; margin-bottom: 0.5em;`,
            },
          },
          {
            resolve: `gatsby-remark-autolink-headers`,
            options: {
              maintainCase: false,
              removeAccents: true,
            },
          },
        ],
      },
    },
    {
      resolve: `@gatsby-contrib/gatsby-plugin-elasticlunr-search`,
      options: {
        // Fields to index
        fields: [`title`, `rawMarkdownBody`],
        // How to resolve each field`s value for a supported node type
        resolvers: {
          // For any node of type MarkdownRemark, list how to resolve the fields` values
          MarkdownRemark: {
            title: (node) => node.frontmatter.title,
            path: (node) => node.frontmatter.path,
            rawMarkdownBody: (node) => node.rawMarkdownBody,
            excerpt: (node) => {
              const excerptLength = 250; // Hard coded excerpt length
              let excerpt = "";
              const tree = remark().parse(node.rawMarkdownBody);
              visit(tree, "text", (node) => {
                excerpt += node.value;
              });
              return excerpt.slice(0, excerptLength) + "...";
            },
            nodeType: (node) => "MarkdownRemark",
          },
          Card: {
            // TODO: Change the name of the field to be its content
            title: (node) => node.name,
            rawMarkdownBody: (node) => node.description,
            path: (node) => node.path,
            collectible: (node) => node.collectible,
            excerpt: (node) => node.description, // is this necessary?
            nodeType: (node) => "Card",
            heroClass: (node) => node.heroClass,
            baseManaCost: (node) => node.baseManaCost,
            art: (node, getNode) => {
              return resolveArt(node, {
                nodeModel: {
                  getNodeById: ({ id }) => getNode(id),
                },
              });
            },
            baseAttack: (node) => node.baseAttack,
            baseHp: (node) => node.baseHp,
            type: (node) => node.type,
            id: (node) => node.id,
          },
          Block: {
            title: (node) => node.searchMessage,
            nodeType: (node) => "Block",
            rawMarkdownBody: (node) => node.comment || "",
          },
        },
        // Optional filter to limit indexed nodes
        filter: (node, getNode) => true,
      },
    },
    {
      resolve: `gatsby-plugin-s3`,
      options: {
        bucketName: "www.playspellsource.com",
        protocol: "https",
        hostname: "www.playspellsource.com",
      },
    },
  ],
};
