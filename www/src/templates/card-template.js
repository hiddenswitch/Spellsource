import React from 'react'
import { graphql } from 'gatsby'
import Layout from '../components/creative-layout'

export default function Template ({
  data, // this prop will be injected by the GraphQL query below.
}) {
  const { card } = data // data.card holds your post data
  const { name, description, type, baseManaCost } = card
  return (
    <Layout>
      <h2>{name} ({baseManaCost})</h2>
      <h3>{type}</h3>
      <p>{description}</p>
    </Layout>
  )
}

export const pageQuery = graphql`
  query($path: String!) {
    card(path: { eq: $path }) {
      name
      baseManaCost
      description
      type
    }
  }
`