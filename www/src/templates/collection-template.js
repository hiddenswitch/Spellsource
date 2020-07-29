import React, { useState } from 'react'
import { graphql, Link } from 'gatsby'
import Layout from '../components/creative-layout'
import CardDisplay from '../components/card-display'
import styles from '../components/creative-layout.module.scss'
import { useIndex } from '../hooks/use-index'

export default function CollectionTemplate ({ data, pageContext }) {
  const cards = data.allCard.edges

  const { currentPage, numPages } = pageContext
  const isFirst = currentPage === 1
  const isLast = currentPage === numPages
  const prevPage = currentPage - 1 === 1 ? '/' : (currentPage - 1).toString()
  const nextPage = (currentPage + 1).toString()

  return (
    <Layout>
      <h2>Collection</h2>
      <div className={styles.collectionDiv}>
        {cards.map(edge => {
          const card = edge.node
          return (
            <Link to={`/cards/${card.id}`} key={card.id}>
              <CardDisplay name={card.name} baseManaCost={card.baseManaCost} description={card.description}
                           art={card.art}
                           baseAttack={card.baseAttack} baseHp={card.baseHp} type={card.type}/>
            </Link>)
        })}
      </div>
      <nav style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
        <div>
          {!isFirst && (
            <Link to={`/collection/${prevPage}`} rel="prev"> ← Previous Page </Link>
          )}
        </div>
        <div style={{ justifySelf: 'flex-end' }}>
          {!isLast && (
            <Link to={`/collection/${nextPage}`} rel="next"> Next Page → </Link>
          )}
        </div>
      </nav>
    </Layout>
  )
}

export const pageQuery = graphql`
  query ($skip: Int!, $limit: Int!) {
    allCard (limit: $limit, skip: $skip) {
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
    }
  }`