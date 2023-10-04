import { ApolloServerPlugin, BaseContext, GraphQLRequestContext, GraphQLRequestListener } from "@apollo/server";
import { CardsInDeck, Deck } from "../__generated__/client";
import redis from "./redis-client";

type DeckIdFields = {
  CardsInDeck: keyof CardsInDeck;
  Deck: keyof Deck;
};

const deckIdFields: DeckIdFields = {
  CardsInDeck: "deckId",
  Deck: "id",
};

type RecordOrArrayThereof = Record<string, unknown> | Record<string, unknown>[];

function extractDeckIdsFromResponse(responseObj: RecordOrArrayThereof): Set<string> {
  const deckIds = new Set<string>();

  if (Array.isArray(responseObj)) {
    for (const item of responseObj) {
      const nestedIds = extractDeckIdsFromResponse(item);
      nestedIds.forEach((id) => deckIds.add(id));
    }
  } else if (responseObj && typeof responseObj === "object") {
    const typeName = responseObj.__typename;
    const deckIdField = deckIdFields[typeName as keyof DeckIdFields];
    if (deckIdField && responseObj[deckIdField]) {
      deckIds.add(responseObj[deckIdField] as string);
    }
    for (const key in responseObj) {
      if (key !== "__typename" && key in responseObj) {
        const responseObjElement = responseObj[key];
        if (typeof responseObjElement === "object" || Array.isArray(responseObjElement)) {
          const resAsRec =
            typeof responseObjElement === "object"
              ? (responseObjElement as Record<string, unknown>)
              : (responseObjElement as Record<string, unknown>[]);
          const nestedIds = extractDeckIdsFromResponse(resAsRec);
          nestedIds.forEach((id) => deckIds.add(id));
        }
      }
    }
  }

  return deckIds;
}

async function handle(requestContext: GraphQLRequestContext<BaseContext>) {
  const { operation } = requestContext;
  const deckIdsSet = new Set<string>();
  if (operation && operation.operation === "mutation") {
    if (!requestContext.response.body) {
      return;
    }
    switch (requestContext.response.body.kind) {
      case "incremental":
        // todo: way too complicated for me to know what to do here
        const docs = [requestContext.response.body.initialResult.data];
        for await (const docArr of requestContext.response.body.subsequentResults) {
          if (!docArr.incremental) {
            continue;
          }
          for (const doc of docArr.incremental) {
            if ("data" in doc && doc.data) {
              docs.push(doc.data);
            }
            if ("items" in doc && doc.items && Array.isArray(doc.items)) {
              for (const subDoc of doc.items) {
                // todo: we are probably streaming object maps here?
                const subItem = subDoc as Record<string, unknown>;
                docs.push(subItem);
              }
            }
          }
        }
        docs.flatMap((doc) => Array.from(extractDeckIdsFromResponse(doc!))).forEach((deckId) => deckIdsSet.add(deckId));
        break;
      case "single":
        if (!requestContext.response.body.singleResult.data) {
          return;
        }
        extractDeckIdsFromResponse(requestContext.response.body.singleResult.data).forEach((deckId) =>
          deckIdsSet.add(deckId)
        );
        break;
    }

    const deckIds = Array.from(deckIdsSet);
    if (deckIds.length > 0) {
      await Promise.all(deckIds.map((deckId) => invalidateDeckId(deckId)));
    }
  }
}

async function invalidateDeckId(deckId: string) {
  // from Legacy.java
  await redis.del(`Spellsource:deck:${deckId}`);
}

export const invalidateDeckPlugin: ApolloServerPlugin = {
  async requestDidStart(): Promise<GraphQLRequestListener<BaseContext> | void> {
    return {
      async willSendResponse(requestContext) {
        await handle(requestContext);
      },
      async willSendSubsequentPayload(requestContext) {
        await handle(requestContext);
      },
    };
  },
};
