import React, { useEffect, useState } from "react";
import PublicSiteLayout, { ContentPanel, EmptyState, PublicPageHeader } from "../components/public-site-layout";
import { ListGroup } from "react-bootstrap";
import { useRouter } from "next/router";
import { isArray } from "lodash";
import { cardSearchNode } from "../hooks/use-index";
import CardDisplay, { CardDef } from "../components/collection/card-display";
import { useGetCollectionCardsQuery } from "../__generated__/client";

const SearchResults = () => {
  const router = useRouter();
  const queryParam = router.query["query"];
  const query = isArray(queryParam) ? queryParam.join("/") : queryParam;

  console.log(query);

  const [offset, setOffset] = useState(0);

  const getCards = useGetCollectionCardsQuery({
    variables: {
      offset,
      limit: 20,
      filter: { id: { includesInsensitive: query } },
    },
  });

  const results = (getCards?.data?.allCollectionCards?.nodes ?? []).map((node) => ({
    ...cardSearchNode(node!.cardScript as CardDef),
    id: node!.id,
  }));

  useEffect(() => {
    console.log(results);
  }, [results]);

  if (results.length === 0) {
    return (
      <PublicSiteLayout title="Search Spellsource">
        <PublicPageHeader eyebrow="Card library" title="Search results" />
        <ContentPanel variant="document">
          <EmptyState title="Nothing found">No cards match “{query}”. Try a different name or keyword.</EmptyState>
        </ContentPanel>
      </PublicSiteLayout>
    );
  } else {
    return (
      <PublicSiteLayout title="Search Spellsource">
        <PublicPageHeader eyebrow="Card library" title="Search results" />
        <ContentPanel variant="document">
          <p>Showing search results for "{query}":</p>
          <ListGroup variant="flush">
            {results.map((page) => (
              <CardDisplay key={page.id} {...page.node} />
            ))}
          </ListGroup>
        </ContentPanel>
      </PublicSiteLayout>
    );
  }
};

export default SearchResults;
