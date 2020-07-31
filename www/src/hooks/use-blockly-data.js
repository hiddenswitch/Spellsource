import { graphql, useStaticQuery } from 'gatsby'

export default function useBlocklyData () {
  return useStaticQuery(graphql`
  query {
    toolbox {
      BlockCategoryList {
        BlockTypePrefix
        CategoryName
        ColorHex
      }
    }
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
          art {
            primary {
              r
              g
              b
            }
          }
        }
      }
    }
    allFile(filter: {extension: {eq: "json"}, relativePath: {glob: "**collectible/**"}}) {
      edges {
        node {
          internal {
            content
          }
          name
        }
      }
    }
  }`)
}