

export default function useBlocklyData () {
  return useStaticQuery(graphql`
  query {
    allBlock {
      edges {
        node {
          id
          args {
            i
            args {
              type
              check
              name
              valueS
              valueI
              valueB
              min
              max
              int
              text
              options
              width
              height
              src
              alt
              variable
              variableTypes
              defaultType
              shadow {
                type
                fields {
                  name
                  valueS
                  valueI
                }
                notActuallyShadow
              }
            }
          }
          inputsInline
          colour
          messages
          nextStatement
          output
          previousStatement
          type
          data
          hat
          comment
          subcategory
          plural
        }
      }
    }
    allCard {
      edges {
        node {
          id
          name
          baseManaCost
          baseAttack
          baseHp
          heroClass
          type
          collectible
          description
          race
          art {
            primary {
              r
              g
              b
            }
            body {
              vertex {
                r
                g
                b
              }
            }
          }
        }
      }
    }
    allJSON: allFile(filter: {extension: {eq: "json"}}) {
      edges {
        node {
          internal {
            content
          }
          name
        }
      }
    }
    allIcon: allFile(filter: {extension: {eq: "png"}, relativePath: {glob: "**assets/editor/**"}}) {
      edges {
        node {
          name
          publicURL
        }
      }
    }
    allArt: allFile(filter: {extension: {eq: "png"}, relativePath: {glob: "**card-images/art/**"}}) {
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
  }`)
}