import React, { useEffect, useRef, useState } from 'react'
import PostLink from './post-link'
import { useStaticQuery, graphql, Link } from 'gatsby'
import Img from 'gatsby-image'
import styles from './creative-layout.module.scss'
import Search from './search'

const Header = () => {
  const data = useStaticQuery(graphql`
  query {
    headerImage: file(relativePath: { eq: "assets/icon.png" }) {
      id
      childImageSharp {
        fixed(width: 25) {
          ...GatsbyImageSharpFixed
        }
      }
    }
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

  const headerDiv = useRef(null)

  const handleScroll = event => {
    sessionStorage.setItem('scrollPosition', headerDiv.current.scrollLeft)
  }

  const keepHorizontalScroll = () => {
    if(sessionStorage.getItem('scrollPosition') !== null) {
      headerDiv.current.scrollLeft = sessionStorage.getItem('scrollPosition')
    }
  }

  useEffect(() => {
    keepHorizontalScroll()
  }, []);

  const pages = data.allMarkdownRemark.edges
    .filter(edge => !!edge.node.frontmatter.header)
    .map(edge => <li key={edge.node.id}><PostLink post={edge.node}/></li>)

  return <header>
    <div className={styles.menu} ref={headerDiv} onScroll={e => {handleScroll(e)}}>
      <ul>
        <li key={data.headerImage.id}><Link to='/'><Img fixed={data.headerImage.childImageSharp.fixed}/></Link></li>
        <li key={'javadocs'}><a href='/javadoc'>[Docs]</a></li>
        {pages}
        <li key={'download'}><Link to='/download'>[Play Now]</Link></li>
        <li key={'search'}><Search placeholder={'Search'} /></li>
      </ul>
    </div>
  </header>
}

export default Header