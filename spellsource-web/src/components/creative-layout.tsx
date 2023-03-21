import React from 'react'
import Header from './header'
import Footer from './footer'
import * as styles from './creative-layout.module.scss'
import Link from "next/link";

export default ({ children }) => {

  const data = {} as any; /*useStaticQuery(graphql`
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
  `)*/

  /*const pages = data.allMarkdownRemark.edges
    .filter(edge => !!edge.node.frontmatter.header)
    .map(edge => <li key={edge.node.id}><PostLink post={edge.node}/></li>)*/

  const pages = [
    <li><Link href={"/wiki"}/>Wiki</li>,
    <li><Link href={"/whats-new"}/>What's New</li>,
    <li><Link href={"/credits"}/>Credits</li>,
  ]

  return <div className={styles.container}>
    <Header pages={pages}/>
    <main>
      {children}
    </main>
    <Footer pages={pages}/>
  </div>
}
