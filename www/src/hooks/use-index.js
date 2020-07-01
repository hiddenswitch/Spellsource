import { createContext, useContext } from 'react'

import { graphql, useStaticQuery } from 'gatsby'
import { Index } from 'elasticlunr'

const IndexContext = createContext({})

// returns index
export const useIndex = () => {
  const data = useStaticQuery(graphql`
  query {
    siteSearchIndex {
      index
    }
  }
`)

  let context = useContext(IndexContext)
  if (!context.index) {
    context.index = Index.load(data.siteSearchIndex.index)
  }

  return context.index
}