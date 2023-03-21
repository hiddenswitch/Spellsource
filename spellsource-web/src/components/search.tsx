import React, { useState, useRef, useEffect } from 'react'
import { Form, FormControl, ListGroup } from 'react-bootstrap'
import * as styles from './creative-layout.module.scss'

import { useIndex } from '../hooks/use-index'
import Link from 'next/link'
import {useRouter} from "next/router";

// Search component
function Search (props) {
  const [query, setQuery] = useState(``)
  const [results, setResults] = useState([])
  const [searchListLeft, setSearchListLeft] = useState(0)

  const router = useRouter();

  function updatePosition () {
    setSearchListLeft(inputBox.current.getBoundingClientRect().left)
  }

  // css sizing for input box
  const inputBox = useRef(null)
  useEffect(() => {
    window.addEventListener('resize', updatePosition)
    updatePosition();
    return () => window.removeEventListener('resize', updatePosition)
  }, []);

  const index = useIndex()

  const dropDownMenu = () => {
    const encoded = encodeURI(query)
    if (encoded.length !== 0) {
      return (
        <ListGroup.Item className={styles.searchListGroupItem}>
          {results.map(page => (
            <ListGroup.Item className={styles.searchListGroupItem} key={page.id}>
              <Link href={page.path}>{page.title}</Link>
            </ListGroup.Item>
          ))}
          <ListGroup.Item className={styles.searchListGroupItem}><Link href={`/searchresults?query=${encoded}`}>
            See more...</Link></ListGroup.Item>
        </ListGroup.Item>
      )
    }
  }

  // update input value
  const updateQuery = event => {
    setQuery(event.target.value)
    window.setTimeout(() => updatePosition(), 10)
  }

  // display full search page on enter
  const navigateToSearchResults = event => {
    event.preventDefault()
    const encoded = encodeURI(query)
    router.push(`../searchresults?query=${encoded}`)
  }

  const search = evt => {
    const query = evt.target.value
    setQuery(query)
    setResults(index
      // Query the index with search string to get an [] of IDs
      .search(query, { expand: true }) // accept partial matches
      // map over each ID and return full document
      .map(({ ref }) => index.documentStore.getDoc(ref))
      .filter(doc => {
        return doc.nodeType === 'Card' || doc.nodeType === 'MarkdownRemark'
      })
      .slice(0, 5)
      // map over each ID and return full document
    )
  }

  return (
    <div className={styles.inputBox}>
      <Form ref={inputBox} onSubmit={e => navigateToSearchResults(e)}>
        <FormControl type="text" placeholder={props.placeholder}
                     value={query}
                     onChange={e => {
                       updateQuery(e)
                       search(e)
                     }}/>
      </Form>
      <ListGroup variant="flush" style={{ left: searchListLeft }} className={styles.searchResults}>
        {dropDownMenu()}
      </ListGroup>
    </div>
  )
}

export default Search
