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
        bucketName: "www.playspellsource.com",
        protocol: "https",
        hostname: "www.playspellsource.com",
      },
    },
  ],
}