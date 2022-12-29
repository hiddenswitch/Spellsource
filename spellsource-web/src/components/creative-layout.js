import React from 'react'
import Header from './header'
import Footer from './footer'
import { useStaticQuery, graphql } from 'gatsby'
import PostLink from './post-link'
import * as styles from './creative-layout.module.scss'

export default ({ children }) => {

  const data = useStaticQuery(graphql`
    query {
      allMarkdownRemark(sort: { order: DESC, fields: [frontmatter___date] }) {
        edges {
          node {
            id
            excerpt(pruneLength: 250)
            frontmatter {
              date(formatString: "MMMM DD, YYYY")
              path
              title
              header
            }
          }
        }
      }
    }
  `)

  const pages = data.allMarkdownRemark.edges
    .filter(edge => !!edge.node.frontmatter.header)
    .map(edge => <li key={edge.node.id}><PostLink post={edge.node}/></li>)

  return <div className={styles.container}>
    <Header pages={pages}/>
    <main>
      {children}
    </main>
    <Footer pages={pages}/>
  </div>
}