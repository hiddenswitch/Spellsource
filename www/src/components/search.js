import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Index } from 'elasticlunr'

import { Link } from 'gatsby'
import { Form, FormControl, ListGroup } from 'react-bootstrap'
import styles from './creative-layout.module.scss'

// Search component
export default class Search extends Component {
  constructor (props) {
    super(props)
    this.state = {
      query: ``,
      results: [],
    }
  }

  render () {
    return (
      <div className={styles.inputBox}>
        <Form>
          <FormControl type="text" placeholder={this.props.placeholder} className="mr-sm-2" value={this.state.query}
                       onChange={this.search}/>
        </Form>
        <ListGroup variant="flush" className={styles.searchResults}>
          {this.state.results.map(page => (
            <ListGroup.Item className={styles.searchListGroupItem} key={page.id}>
                <Link to={page.path}>{page.collectible === false ? <del>{page.title}</del> : page.title}</Link>
            </ListGroup.Item>
          ))}
        </ListGroup>
      </div>
    )
  }

  getOrCreateIndex = () =>
    this.index
      ? this.index
      : // Create an elastic lunr index and hydrate with graphql query results
      Index.load(this.props.searchIndex)

  search = evt => {
    const query = evt.target.value
    this.index = this.getOrCreateIndex()
    this.setState({
      query,
      // Query the index with search string to get an [] of IDs
      results: this.index
        .search(query, { expand: true })
        .slice(0, 5)
        // Map over each ID and return the full document
        .map(({ ref }) => this.index.documentStore.getDoc(ref)),
    })
  }
}

Search.propTypes = {
  searchIndex: PropTypes.object,
}