import React from 'react'
import PostLink from './post-link'
import { useStaticQuery, graphql, Link } from 'gatsby'
import Img from 'gatsby-image'

const Header = () => {
  const data = useStaticQuery(graphql`
  query {
    headerImage: file(relativePath: { eq: "assets/icon.png" }) {
      childImageSharp {
        fixed(width: 53) {
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
  const pages = data.allMarkdownRemark.edges
    .filter(edge => !!edge.node.frontmatter.date && !!edge.node.frontmatter.header)
    .map(edge => <li><PostLink key={edge.node.id} post={edge.node}/></li>)

  return <header>
    <div className="menu">
      <ul>
        <li><Link to='/'><Img fixed={data.headerImage.childImageSharp.fixed}/></Link></li>
        {pages}</ul>
    </div>
  </header>
}

export default Header