import React from 'react'
import Layout from '../components/creative-layout'
import CardDisplay from '../components/card-display'
import * as BlocklyMiscUtils from '../lib/blockly-misc-utils'
import * as styles from '../templates/template-styles.module.scss';
import Link from 'next/link';

export default function Template ({
  data, // this prop will be injected by the GraphQL query below.
}) {
  const { card } = data // data.card holds your post data
  const { name, description, type, baseManaCost, baseAttack, baseHp, rarity, art, id} = card
  let typeAndStats;
  if (type === "MINION") {
    typeAndStats = "(" + baseAttack + ", " + baseHp + ") " + type;
  } else {
    typeAndStats = type;
  }

  let artURL = BlocklyMiscUtils.getArtURL(card, data)
  if (!!artURL) {
    card.art.sprite.named = artURL
  }

  return (
    <Layout>
      <div className={styles.templateContainer}>
        <h2>{name} ({baseManaCost})</h2>
        <p>{rarity}</p>
        <div>
          <CardDisplay name={name} baseManaCost={baseManaCost} description={description} art={art} baseAttack={baseAttack} baseHp={baseHp} type={type}/>
        </div>
        <h3>{typeAndStats}</h3>
        <p>{description}</p>
        <Link href={'/card-editor?card=' + card.id}>Open in Card Editor</Link>
      </div>
    </Layout>
  )
}

// once sprites set, need to add a field to query for sprite

/*
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
      id
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
        sprite {
          named
        }
      }
    }
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
  }
`*/
