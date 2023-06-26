import React, { useState } from "react";
import Layout from "../components/creative-layout";
import CardDisplay from "../components/card-display";
import * as styles from "../components/creative-layout.module.scss";
import { useIndex } from "../hooks/use-index";
import { Form, FormControl } from "react-bootstrap";
import Link from "next/link";

export default function CollectionTemplate({ data, pageContext }) {
  const cards = data.allCard.edges;

  const { currentPage, numPages } = pageContext;
  const isFirst = currentPage === 1;
  const isLast = currentPage === numPages;
  const prevPage = currentPage - 1 === 1 ? "/" : (currentPage - 1).toString();
  const nextPage = (currentPage + 1).toString();

  const index = useIndex();
  const [query, setQuery] = useState(``);
  const [results, setResults] = useState([]);

  // returns all cards matching the search criteria
  const searchCards = (evt) => {
    const query = evt.target.value;
    setQuery(query);
    setResults(
      index
        // Query the index with search string to get an [] of IDs
        .search(query, { expand: true }) // accept partial matches
        // map over each ID and return document
        .map(({ ref }) => index.documentStore.getDoc(ref))
        .filter((doc) => {
          return doc.nodeType === "Card";
        })
        .slice(0, 15)
    );
  };

  const checkQuery = () => {
    if (query !== "") {
      return (
        <div className={styles.collectionDiv}>
          {results.map((card) => {
            return (
              <Link href={card.path} key={card.id}>
                <CardDisplay
                  name={card.title}
                  baseManaCost={card.baseManaCost}
                  description={card.rawMarkdownBody}
                  art={card.art}
                  baseAttack={card.baseAttack}
                  baseHp={card.baseHp}
                  type={card.type}
                />
              </Link>
            );
          })}
        </div>
      );
    } else {
      return (
        <div className={styles.collectionDiv}>
          {cards.map((edge) => {
            const card = edge.node;
            return (
              <Link href={`/cards/${card.id}`} key={card.id}>
                <CardDisplay
                  name={card.name}
                  baseManaCost={card.baseManaCost}
                  description={card.description}
                  art={card.art}
                  baseAttack={card.baseAttack}
                  baseHp={card.baseHp}
                  type={card.type}
                />
              </Link>
            );
          })}
        </div>
      );
    }
  };

  const checkForNav = () => {
    if (query === "") {
      return (
        <nav style={{ display: "flex", justifyContent: "space-between", marginTop: "20px" }}>
          <div>
            {!isFirst && (
              <Link href={`/collection/${prevPage}`} rel="prev">
                {" "}
                ← Previous Page{" "}
              </Link>
            )}
          </div>
          <div style={{ justifySelf: "flex-end" }}>
            {!isLast && (
              <Link href={`/collection/${nextPage}`} rel="next">
                {" "}
                Next Page →{" "}
              </Link>
            )}
          </div>
        </nav>
      );
    }
  };

  return (
    <Layout>
      <h2>Collection</h2>
      <div className={styles.inputBox}>
        <Form>
          <FormControl
            type="text"
            placeholder="Search"
            value={query}
            onChange={(e) => {
              searchCards(e);
            }}
          />
        </Form>
      </div>
      <br />
      {checkQuery()}
      {checkForNav()}
    </Layout>
  );
}

/*
export const pageQuery = graphql`
  query ($skip: Int!, $limit: Int!) {
    allCard (limit: $limit, skip: $skip) {
      edges {
        node {
          id
          name
          baseManaCost
          baseAttack
          baseHp
          heroClass
          type
          collectible
          description
          art {
            body {
              vertex {
                r
                g
                b
                a
              }
            }
            highlight {
              r
              g
              b
              a
            }
            primary {
              r
              g
              b
              a
            }
            secondary {
              r
              g
              b
              a
            }
            shadow {
              r
              g
              b
              a
            }
          }
        }
      }
    }
  }`*/
