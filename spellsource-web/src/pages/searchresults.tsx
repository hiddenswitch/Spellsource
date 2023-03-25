import React, {useEffect, useState} from 'react'
import * as styles from '../components/creative-layout.module.scss'
import * as templateStyles from '../templates/template-styles.module.scss'
import Layout from '../components/creative-layout'
import {ListGroup} from 'react-bootstrap'
import {useRouter} from 'next/router'
import {isArray} from "lodash";
import {useGetCardsQuery} from "../__generated__/client";
import {cardSearchNode} from "../hooks/use-index";
import CardDisplay from "../components/card-display";

const SearchResults = () => {
  const router = useRouter();
  const queryParam = router.query["query"]
  const query = isArray(queryParam) ? queryParam.join("/") : queryParam;

  console.log(query);

  const [offset, setOffset] = useState(0);

  const getCards = useGetCardsQuery({
    variables: {
      offset,
      limit: 20,
      filter: {id: {includesInsensitive: query}}
    }
  })


  const results = (getCards?.data?.allCards?.nodes ?? []).map((node) => ({
    ...cardSearchNode(node.cardScript),
    id: node.id
  }));


  useEffect(() => {
    console.log(results)
  }, [results])

  if (results.length === 0) {
    return (
      <Layout>
        <p>Showing search results for "{query}":</p>
        <p>Nothing found</p>
      </Layout>
    )
  } else {
    return (
      <Layout>
        <div className={templateStyles.templateContainer}>
          <p>Showing search results for "{query}":</p>
          <ListGroup variant="flush" className={styles.searchResults}>
            {results.map(page => (
              <CardDisplay key={page.id} {...page.node}/>
            ))}
          </ListGroup>
        </div>
      </Layout>
    )
  }
}

export default SearchResults
