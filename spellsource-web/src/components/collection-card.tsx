import React, { FunctionComponent} from 'react'
import CardDisplay, {CardDef} from './card-display'
import * as styles from './collection-card.module.scss'
import useArtData from '../hooks/use-art-data'
import * as BlocklyMiscUtils from '../lib/blockly-misc-utils'
import {DeepPartial} from "../lib/deep-partial";
import Link from "next/link";

interface CollectionCardProps {
  card: DeepPartial<CardDef>
}

const CollectionCard: FunctionComponent<CollectionCardProps> = (props) => {
  const data = useArtData()

  let card = props.card


  let art = {
    ...card.art
  }

  return <span className={styles.span}>
    <CardDisplay
      name={card.name}
      baseManaCost={card.baseManaCost}
      description={card.description}
      art={art}
      baseAttack={card.baseAttack}
      baseHp={card.baseHp}
      type={card.type}
    />
    <Link href={'/card-editor?card=' + card.id}>Open in Card Editor</Link>
  </span>
}

export default CollectionCard
