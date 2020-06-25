import React from 'react'

import styles from '../components/creative-layout.module.scss'
import queryString from 'query-string'
import Layout from '../components/creative-layout'
import { ListGroup } from 'react-bootstrap'
import { Link } from 'gatsby'

import { useIndex } from '../hooks/use-index'

const SearchResults = ({ location }) => {
  let parsed = queryString.parse(decodeURI(location.search))
  const query = parsed.query

  let index = useIndex()
  const results = index
    .search(query, { expand: true })
    .map(({ ref }) => index.documentStore.getDoc(ref))
    .filter(doc => {
      return doc.nodeType === 'Card' || doc.nodeType === 'MarkdownRemark'
    })

  if (results.length === 0) {
    return (
      <Layout>
        <p>Showing search results for ``{query}":</p>
        <p>Nothing found</p>
      </Layout>
    )
  } else {
    return (
      <Layout>
        <p>Showing search results for ``{query}":</p>
        <ListGroup variant="flush" className={styles.searchResults}>
          {results.map(page => (
            <ListGroup.Item className={styles.searchListGroupItem} key={page.id}>
              <Link to={page.path}>{page.title}</Link>
              <p>{page.excerpt}</p>
            </ListGroup.Item>
          ))}
        </ListGroup>
      </Layout>
    )
  }
}

export default SearchResults