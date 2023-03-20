import Layout from '../components/card-editor-layout'
import React from 'react'
import CollectionCard from '../components/collection-card'
import {CardProps} from "../components/card-display";
import {DeepPartial} from "../lib/deep-partial";

const cards: Array<DeepPartial<CardProps>> = [
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL',
    art: {
      sprite: {
        named: 'Mothman'
      }
    },
    id: 'minion_alien_ravager'
  },
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL'
  },
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL'
  },
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL'
  },
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL'
  },
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL'
  },
  {
    name: 'Test',
    description: 'Test Description',
    baseManaCost: 0,
    type: 'SPELL'
  }
]

const MyCards = () => {
  return <Layout>
    <div>
      {
        cards.map(card => <CollectionCard card={card}/>)
      }
    </div>
  </Layout>
}



export default MyCards