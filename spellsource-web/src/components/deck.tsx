import React, { FunctionComponent } from "react"
import {DeckCardsFragment, DeckFragment, useGetDeckQuery} from "../__generated__/client"
import { CardDef } from "./card-display"
import { chain } from "lodash"

interface DeckProps {
  deckId: string;
  classColors: Record<string, string>
}

export const Deck: FunctionComponent<DeckProps> = ({ deckId }) => {

  return <></>
}
