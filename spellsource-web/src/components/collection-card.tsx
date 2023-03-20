import React, {cloneElement, FunctionComponent} from 'react'
import CardDisplay, {CardProps} from './card-display'
import { Button } from 'react-bootstrap'
import * as styles from './collection-card.module.scss'
import useArtData from '../hooks/use-art-data'
import BlocklyMiscUtils from '../lib/blockly-misc-utils'
import { Link } from 'gatsby'
import {DeepPartial} from "../lib/deep-partial";

interface CollectionCardProps {
  card: DeepPartial<CardProps>
}

const CollectionCard: FunctionComponent<CollectionCardProps> = (props) => {
  const data = useArtData()

  let card = props.card

  let artURL = BlocklyMiscUtils.getArtURL(card, data)

  let art = {
    ...card.art
  }

  if (artURL != null) {
    art.sprite.named = artURL
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
    <Link to={'/card-editor?card=' + card.id}>Open in Card Editor</Link>
  </span>
}

export default CollectionCard