import React from 'react'
import { graphql } from 'gatsby'
import Layout from '../components/creative-layout'
import CardDisplay from '../components/card-display'

export default function Template ({
  data, // this prop will be injected by the GraphQL query below.
}) {
  const { card } = data // data.card holds your post data
  const { name, description, type, baseManaCost, baseAttack, baseHp, rarity, art } = card
  let typeAndStats;
  if (type === "MINION") {
    typeAndStats = "(" + baseAttack + ", " + baseHp + ") " + type;
  } else {
    typeAndStats = type;
  }

  return (
    <Layout>
      <h2>{name} ({baseManaCost})</h2>
      <p>{rarity}</p>
      <div style={{ marginBottom: '190px' }}>
        <CardDisplay name={name} baseManaCost={baseManaCost} description={description} art={art} baseAttack={baseAttack} baseHp={baseHp} type={type}/>
      </div>
      <h3>{typeAndStats}</h3>
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
      rarity
      baseAttack
      baseHp
      art {
        body {
          vertex {
            r
            g
            b
            a
          }
        }
        highlight {
          r
          g
          b
          a
        }
        primary {
          r
          g
          b
          a
        }
        secondary {
          r
          g
          b
          a
        }
        shadow {
          r
          g
          b
          a
        }
      }
    }
  }
`