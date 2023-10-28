import React, { FunctionComponent, useContext } from "react";
import _, { isEqual } from "lodash";
import { textDecorationStyle } from "./collection";
import { Button } from "react-bootstrap";
import { useDrag, useDrop } from "react-dnd";
import {
  GetDeckQuery,
  GetDeckQueryVariables,
  GetDecksQuery,
  GetDecksQueryVariables,
  useCreateDeckMutation,
  useDeleteDeckMutation,
  useRenameDeckMutation,
  useSetCardsInDeckMutation,
} from "../../__generated__/client";
import { ListActions } from "react-use/lib/useList";
import { useRouter } from "next/router";
import { CardCache } from "../../pages/collection";
import { CardDef } from "./card-display";
import { QueryResult } from "@apollo/client";

export type GetDeckQueryDeck = GetDeckQuery["deckById"];
interface DeckProps {
  deck: GetDeckQueryDeck;
  user?: string;
  myDeck: boolean;
  cardIds: string[];
  classColors: Record<string, string | undefined>;
  cardActions: ListActions<string>;
  getDeck: QueryResult<GetDeckQuery, GetDeckQueryVariables>;
  getDecks: QueryResult<GetDecksQuery, GetDecksQueryVariables>;
  setDeckId: (deckId: string) => Promise<boolean>;
  realCards: string[];
}

function DeckCard(props: {
  cardDefs: CardDef[];
  card: CardDef;
  heroClass: any;
  myDeck: boolean;
  classColors: any;
  onClick: () => void;
}) {
  const { card, myDeck } = props;

  const [, dragRef] = useDrag({
    type: "deck-card",
    item: { id: card.id },
    canDrag: () => myDeck,
  });

  return (
    <li className={"d-flex flex-row align-items-baseline"}>
      <span className={`${props.cardDefs.length + 1 > (props.card.rarity === "LEGENDARY" ? 1 : 2) && "text-danger"}`}>
        {props.cardDefs.length + 1} x
      </span>
      <Button
        ref={dragRef}
        variant={"light"}
        className={`ms-2 ${
          props.card.heroClass !== "ANY" && props.card.heroClass !== props.heroClass && "text-danger"
        }`}
        style={{
          pointerEvents: props.myDeck ? "initial" : "none",
          ...textDecorationStyle(props.card.heroClass, props.classColors),
        }}
        onClick={props.onClick}
      >
        ({props.card.baseManaCost ?? 0}) {props.card.name}
      </Button>
    </li>
  );
}

export const Deck: FunctionComponent<DeckProps> = ({
  deck,
  user,
  myDeck,
  cardIds,
  classColors,
  cardActions,
  getDecks,
  getDeck,
  setDeckId,
  realCards,
}) => {
  const router = useRouter();

  const cache = useContext(CardCache);

  const groupedCards = _.chain(cardIds)
    .map((value) => cache[value])
    .filter((value) => !!value)
    .groupBy((card) => card.id)
    .sortBy(
      ([card]) => card.baseManaCost ?? 0,
      ([card]) => card.name
    )
    .value();

  const { set: setCards, removeAt: removeCard, push: addCardToDeck } = cardActions;

  const [setCardsInDeck] = useSetCardsInDeckMutation();
  const [deleteDeck] = useDeleteDeckMutation();
  const [renameDeck] = useRenameDeckMutation();
  const [createDeck] = useCreateDeckMutation();

  const [, deckDrop] = useDrop({
    accept: ["collection-card"],
    drop: (item) => {
      if (typeof item === "object" && item != null && "id" in item) {
        const itemObj = item as Record<string, string>;
        addCardToDeck(itemObj["id"]);
      }
    },
  });

  return (
    <div ref={deckDrop}>
      <h3 className={"d-flex flex-row gap-2 align-items-baseline flex-wrap"}>
        <span style={textDecorationStyle(deck?.heroClass, classColors)}>{deck?.name ?? "Your Decks"}</span>
        <span className={`ms-auto ${cardIds.length > 30 && "text-danger"}`}>{cardIds.length}/30</span>
        <Button
          className={"ms-auto"}
          variant={"secondary"}
          onClick={async () => {
            if (isEqual(cardIds, realCards) || confirm("Leave and discard changes?")) {
              await router.push({
                query: {
                  ...router.query,
                  deckId: undefined,
                  heroClass: undefined,
                },
              });
            }
          }}
        >
          Back
        </Button>
      </h3>
      <div className={"d-flex flex-row gap-3 flex-wrap mb-3"}>
        {user && (
          <Button
            onClick={async () => {
              // todo: don't use prompt if possible
              const deckName = prompt("New Deck Name", deck?.name ?? "") || "Duplicate of " + deck?.name;

              const { data } = await createDeck({
                variables: {
                  deckName,
                  heroClass: deck?.heroClass ?? "",
                  cardIds: deck?.cardsInDecksByDeckId.nodes.map((value) => value!.cardId),
                  format: deck?.format ?? "",
                },
              });
              const newDeckId = data?.createDeckWithCards?.deck?.id;
              if (newDeckId) {
                await getDecks.refetch();
                await setDeckId(newDeckId);
              }
            }}
          >
            Duplicate
          </Button>
        )}
        {myDeck && (
          <>
            <Button
              variant={"light"}
              onClick={async () => {
                let newName = prompt("New Deck Name");
                if (!newName) return;

                const { data } = await renameDeck({
                  variables: {
                    deckId: deck!.id,
                    deckName: newName,
                  },
                });
                if (data!.updateDeckById?.deck?.name) {
                  await getDecks.refetch();
                }
              }}
            >
              Rename
            </Button>
            <Button
              disabled={isEqual(cardIds, realCards) || getDeck.loading}
              variant={"light"}
              onClick={() => setCards(realCards)}
            >
              Reset
            </Button>
            <Button
              disabled={isEqual(cardIds, realCards) || getDeck.loading}
              onClick={async () => {
                const { data } = await setCardsInDeck({
                  variables: {
                    cardIds,
                    deckId: deck!.id,
                  },
                });
                if (data?.setCardsInDeck?.cardsInDecks) {
                  await getDeck.refetch();
                }
              }}
            >
              Save
            </Button>
            <Button
              variant={"danger"}
              onClick={async () => {
                if (!confirm(`Are you sure you want to delete "${deck!.name}"?`)) return;

                const { data } = await deleteDeck({ variables: { deckId: deck!.id } });
                if (data!.updateDeckById?.deck?.trashed) {
                  await getDecks.refetch();
                  await router.replace({
                    query: {
                      ...router.query,
                      deckId: undefined,
                      heroClass: undefined,
                    },
                  });
                }
              }}
            >
              Delete
            </Button>
          </>
        )}
      </div>
      <ul className={"d-flex flex-column gap-2"}>
        {groupedCards.map(([card, ...rest]) => (
          <DeckCard
            key={card.id}
            cardDefs={rest}
            card={card}
            heroClass={deck?.heroClass}
            myDeck={myDeck}
            classColors={classColors}
            onClick={() => {
              const index = cardIds.findIndex((value) => value == card.id);
              if (index >= 0) {
                removeCard(index);
              }
            }}
          />
        ))}
      </ul>
    </div>
  );
};
