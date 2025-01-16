import React, { createContext, useMemo, useRef } from "react";
import Layout from "../components/card-editor-layout";
import * as styles from "../templates/template-styles.module.scss";
import { GetStaticPropsContext, InferGetStaticPropsType } from "next";
import { getAllArt, getAllBlockJson, getAllIcons } from "../lib/fs-utils";
import { CardDef } from "../components/collection/card-display";
import { fixArt } from "../lib/json-transforms";
import { keyBy } from "lodash";
import { Card, GeneratedArtFragment, GetCardsQuery, GetGeneratedArtQuery, useGetCardsQuery, useGetClassesQuery, useGetCollectionCardsLazyQuery, useGetGeneratedArtQuery, useSaveGeneratedArtMutation } from "../__generated__/client";
import { useSession } from "next-auth/react";
import { ApolloQueryResult } from "@apollo/client";
import Head from "next/head";
import { Spinner } from "react-bootstrap";
import CardEditorWorkspace from "../components/card-editor-workspace";
import SpellsourceRenderer from "../lib/spellsource-renderer";
import { ImageDef } from "../lib/art-generation";

export const getStaticProps = async (context: GetStaticPropsContext) => {
  const allBlocks = await getAllBlockJson();
  const blocksByType = keyBy(allBlocks, (block) => block.type);
  const allArt = await getAllArt();
  const allIcons = await getAllIcons();

  return {
    props: { allBlocks, allIcons, blocksByType, allArt },
  };
};

export const BlocklyDataContext = createContext({ ready: false } as InferGetStaticPropsType<typeof getStaticProps> & {
  ready?: boolean;
  classes: Record<string, CardDef>;
  allArt: ImageDef[];
  generatedArt?: GeneratedArtFragment[];
  myCards: Partial<Card>[];
  refreshMyCards?: () => Promise<ApolloQueryResult<GetCardsQuery>>;
  userId: string | null | undefined;
  getCollectionCards?: ReturnType<typeof useGetCollectionCardsLazyQuery>[0];
  saveGeneratedArt?: ReturnType<typeof useSaveGeneratedArtMutation>[0];
  refreshGeneratedArt?: () => Promise<ApolloQueryResult<GetGeneratedArtQuery>>;
});

const CardEditor = (props: InferGetStaticPropsType<typeof getStaticProps>) => {
  const { data: session } = useSession();
  const userId = session?.token?.sub ?? "";

  const getClasses = useGetClassesQuery({
    variables: {
      filter: {
        createdBy: { notEqualToInsensitive: userId },
        collectible: { equalTo: true },
      },
    },
  });
  const getMyCards = useGetCardsQuery({
    variables: {
      filter: {
        isPublished: { equalTo: false },
      },
    },
  });

  const [getCollectionCards] = useGetCollectionCardsLazyQuery();
  const [saveGeneratedArt] = useSaveGeneratedArtMutation();

  const classes = useMemo(() => {
    const cards = getClasses.data?.allClasses?.nodes ?? [];
    const allCards = cards.map(
      (card) =>
        ({
          ...(card!.cardScript ?? {}),
          id: card!.id,
        }) as CardDef
    );
    const cardsById = keyBy(allCards, (card) => card.id);
    fixArt(cardsById);

    return cardsById;
  }, [getClasses.data]);

  const getGeneratedArt = useGetGeneratedArtQuery();
  const generatedArt = (getGeneratedArt.data?.allGeneratedArts?.nodes ?? []).flatMap((f) => (!!f ? [f] : []));

  const ready = Object.values(classes).length > 0 && getGeneratedArt.data && typeof window !== "undefined";

  const myCards = useMemo(() => (getMyCards.data?.allCards?.nodes ?? []).flatMap((card) => (card?.blocklyWorkspace ? [card] : [])), [getMyCards.data]);

  const refreshMyCards = getMyCards.refetch; // TODO get .reobserve working
  const refreshGeneratedArt = getGeneratedArt.refetch;

  const compactBlocks = true;
  const blocklyEditor = useRef(null);

  return (
    <Layout>
      <Head>
        <title>Spellsource Card Editor</title>
      </Head>
      <BlocklyDataContext.Provider
        value={{
          ...props,
          classes,
          ready,
          myCards,
          refreshMyCards,
          userId,
          getCollectionCards,
          saveGeneratedArt,
          generatedArt,
          refreshGeneratedArt,
        }}
      >
        <div className={styles.cardEditorContainer}>
          {ready ? (
            <CardEditorWorkspace ref={blocklyEditor} defaultCard={false} renderer={compactBlocks ? SpellsourceRenderer.name : "geras"} />
          ) : (
            <div className={"h-100 w-100 d-flex"}>
              <Spinner variant={"placeholder"} className={"m-auto"} />
            </div>
          )}
        </div>
      </BlocklyDataContext.Provider>
    </Layout>
  );
};

export default CardEditor;
