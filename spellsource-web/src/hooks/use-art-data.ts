export default function useBlocklyData() {
  return {} as any;
  /*return useStaticQuery(graphql`
  query {
    allArt: allFile(filter: {extension: {eq: "png"}, relativePath: {glob: "**card-images/art/!**"}}) {
      edges {
        node {
          name
          childImageSharp {
            fluid {
              presentationHeight
              presentationWidth
              src
            }
          }
        }
      }
    }
  }`)*/
}