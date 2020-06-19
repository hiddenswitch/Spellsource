import React, { useState } from 'react'

import { Link, navigate } from 'gatsby'
import { Form, FormControl, ListGroup } from 'react-bootstrap'
import styles from './creative-layout.module.scss'

import { useIndex } from '../hooks/use-index'

// Search component
function Search (props) {
  const [query, setQuery] = useState(``)
  const [results, setResults] = useState([])

  const index = useIndex()

  const dropDownMenu = () => {
    const encoded = encodeURI(query)
    if (encoded.length !== 0) {
      return (
        <ListGroup.Item className={styles.searchListGroupItem}>
          <Link to={`../searchresults?query=${encoded}`}>
            See more...</Link>
        </ListGroup.Item>
      )
    }
  }

  // update input value
  const updateQuery = event => {
    setQuery(event.target.value)
  }

  // display full search page on enter
  const navigateToSearchResults = event => {
    event.preventDefault()
    const encoded = encodeURI(query)
    navigate(`../searchresults?query=${encoded}`)
  }

  const search = evt => {
    const query = evt.target.value
    setQuery(query)
    setResults(index
      // Query the index with search string to get an [] of IDs
      .search(query, { expand: true }) // accept partial matches
      .slice(0, 5)
      // map over each ID and return full document
      .map(({ ref }) => index.documentStore.getDoc(ref)))
  }

  return (
    <div className={styles.inputBox}>
      <Form onSubmit={e => navigateToSearchResults(e)}>
        <FormControl type="text" placeholder={props.placeholder} className="mr-sm-2"
                     value={query}
                     onChange={e => {
                       updateQuery(e)
                       search(e)
                     }}/>
      </Form>
      <ListGroup variant="flush" className={styles.searchResults}>
        {results.map(page => (
          <ListGroup.Item className={styles.searchListGroupItem} key={page.id}>
            <Link to={page.path}>{page.title}</Link>
          </ListGroup.Item>
        ))}
        {dropDownMenu()}
      </ListGroup>
    </div>
  )
}

export default Search