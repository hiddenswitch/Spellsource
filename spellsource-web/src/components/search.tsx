import React, {ChangeEvent, FormEvent, FunctionComponent, useEffect, useMemo, useRef, useState} from "react";
import {Form, FormControl, ListGroup} from "react-bootstrap";
import * as styles from "./creative-layout.module.scss";

import {SearchNode, useIndex} from "../hooks/use-index";
import Link from "next/link";
import {useRouter} from "next/router";
import {useDebounce} from "react-use";
import {useGetCollectionCardsLazyQuery} from "../__generated__/client"; // Search component

// Search component
const Search: FunctionComponent<{
  placeholder: string;
}> = (props) => {
  const [query, setQuery] = useState(``);
  const [results, setResults] = useState([] as SearchNode[]);
  const [searchListLeft, setSearchListLeft] = useState(0);

  const router = useRouter();

  function updatePosition() {
    if (!inputBox.current) {
      return;
    }
    setSearchListLeft(inputBox.current.getBoundingClientRect().left);
  }

  // css sizing for input box
  const inputBox = useRef<HTMLFormElement>(null);
  useEffect(() => {
    window.addEventListener("resize", updatePosition);
    updatePosition();
    return () => window.removeEventListener("resize", updatePosition);
  }, []);

  const index = useIndex();

  const dropDownMenu = useMemo(() => {
    const encoded = encodeURI(query);
    if (encoded.length !== 0) {
      return (
        <ListGroup.Item className={styles.searchListGroupItem}>
          {results.map((page) => (
            <ListGroup.Item className={styles.searchListGroupItem} key={page.id}>
              <Link href={page.path}>{page.title}</Link>
            </ListGroup.Item>
          ))}
          <ListGroup.Item className={styles.searchListGroupItem}>
            <Link href={`/searchresults?query=${encoded}`}>See more...</Link>
          </ListGroup.Item>
        </ListGroup.Item>
      );
    }
  }, [query, results]);

  // update input value
  const updateQuery = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setQuery(event.target!.value);
    window.setTimeout(() => updatePosition(), 10);
  };

  // display full search page on enter
  const navigateToSearchResults = (event: FormEvent) => {
    event.preventDefault();
    const encoded = encodeURI(query);
    router.push(`../searchresults?query=${encoded}`);
  };

  const search = (evt: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    /*const query = evt.target.value
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
                )*/
  };

  const [getCards] = useGetCollectionCardsLazyQuery();

  useDebounce(
    async () => {
      if (!query) {
        setResults([]);
        return;
      }

      const { data } = await getCards({ variables: { limit: 5, filter: { id: { includesInsensitive: query } } } });
    },
    500,
    [query]
  );

  const [focused, setFocused] = useState(false);

  return (
    <Form
      ref={inputBox}
      onSubmit={(e) => navigateToSearchResults(e)}
      onFocus={(event) => setFocused(true)}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) {
          setFocused(false);
        }
      }}
    >
      <FormControl
        className={styles.inputBox}
        type="text"
        placeholder={props.placeholder}
        value={query}
        onChange={(e) => {
          updateQuery(e);
          search(e);
        }}
      />
      {focused && (
        <ListGroup variant="flush" style={{ left: searchListLeft }} className={styles.searchResults}>
          {dropDownMenu}
        </ListGroup>
      )}
    </Form>
  );
};

export default Search;
