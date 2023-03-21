import React from 'react'
import * as styles from './card-display.module.scss'

import backgroundLayer from '../../public/static/card-images/layer-1.png'
import whiteBanner from '../../public/static/card-images/layer-3.png'
import highlight from '../../public/static/card-images/layer-4.png'
import shadow from '../../public/static/card-images/layer-5.png'
import secondary from '../../public/static/card-images/layer-6.png'
import baseAttack from '../../public/static/card-images/attack-token.png'
import baseHp from '../../public/static/card-images/health-token.png'

import pedestalPrimary from '../../public/static/card-images/pedestal-primary.png'
import pedestalSecondary from '../../public/static/card-images/pedestal-secondary.png'
import pedestalShadow from '../../public/static/card-images/pedestal-shadow.png'
import windowBackground from '../../public/static/card-images/large-card-window-background.png'
import selkie from '../../public/static/card-images/selkie.png'
import selkieShadow from '../../public/static/card-images/selkie-shadow.png'
import {defaultsDeep} from 'lodash'
import {CardDesc} from "../lib/spellsource-game";
import {DeepPartial} from "../lib/deep-partial";

const defaultArt = {
  'primary': {
    'r': 0.443,
    'g': 0.396,
    'b': 0.509,
    'a': 1.0
  },
  'secondary': {
    'r': 0.207,
    'g': 0.282,
    'b': 0.466,
    'a': 1.0
  },
  'shadow': {
    'r': 0.207,
    'g': 0.282,
    'b': 0.466,
    'a': 1.0
  },
  'highlight': {
    'r': 0.768,
    'g': 0.67,
    'b': 0.764,
    'a': 1.0
  },
  'body': {
    'vertex': {
      'r': 1.0,
      'g': 1.0,
      'b': 1.0,
      'a': 1.0
    }
  },
  'sprite': {
    'named': selkie,
    'shadow': selkieShadow
  }
}

const toRgbaString = (v: { r: number; g: number; b: number; a: any }) => {
  return `rgba(${v.r * 255}, ${v.g * 255}, ${v.b * 255}, ${v.a})`
}

type FixCardDesc<T> = T extends object ? {
  [K in keyof T as K extends `${infer name}_` ? name : K]: K extends "unknownFields" ? never : FixCardDesc<T[K]>
} : T

export type CardProps = FixCardDesc<CardDesc>;

type DeepReplace<T, V, N> = T extends object ? {
  [K in keyof T]: T[K] extends V ? N : DeepReplace<T[K], V, N>
} : T;

function CardDisplay(props: DeepPartial<CardProps>) {
  let cardArt = defaultsDeep(props.art, defaultArt) as CardProps["art"] & typeof defaultArt
  const art = {
    ...cardArt,
    body: {
      ...cardArt.body,
      vertex: toRgbaString(cardArt.body.vertex)
    },
    highlight: toRgbaString(cardArt.highlight),
    primary: toRgbaString(cardArt.primary),
    secondary: toRgbaString(cardArt.secondary),
    shadow: toRgbaString(cardArt.shadow),
    sprite: {
      // named: art.sprite.named,
      ...cardArt.sprite
    }
  }

  const checkTextColor = () => {
    if (art.body.vertex === `rgba(0, 0, 0, 1)`) {
      return (`none`)
    } else {
      return (`-2px 2px #21215c`)
    }
  }

  const checkTokens = () => {
    if ((props.baseAttack === null || props.baseAttack === undefined) && (props.baseHp === null || props.baseHp === undefined)) {
      return (`none`)
    } else {
      return (`initial`)
    }
  }

  return (
    <div className={styles.cardDisplayTemplate}>
      <img src={backgroundLayer} className={styles.layerOne} alt='card'/>
      <div className={styles.descriptionBox}>
        <p className={styles.description} style={{
          color: art.body.vertex,
          textShadow: checkTextColor()
        }}>{props.description}</p>
        <p className={styles.type}>{props.type}</p>
      </div>
      <p className={styles.baseManaCost}>{props.baseManaCost}</p>
      <p className={styles.name}
         style={{
           color: art.body.vertex,
           textShadow: checkTextColor()
         }}>{props.name}</p>
      <div className={styles.primary}
           style={{background: `linear-gradient(${art.primary}, ${art.primary}), url(${whiteBanner}) no-repeat`}}/>
      <div className={styles.highlight}
           style={{background: `linear-gradient(${art.highlight}, ${art.highlight}), url(${highlight}) no-repeat`}}/>
      <div className={styles.shadow}
           style={{background: `linear-gradient(${art.shadow}, ${art.shadow}), url(${shadow}) no-repeat`}}/>
      <div className={styles.secondary}
           style={{background: `linear-gradient(${art.secondary}, ${art.secondary}), url(${secondary}) no-repeat`}}/>
      <div className={styles.pedestalPrimary}
           style={{background: `linear-gradient(${art.primary}, ${art.primary}), url(${pedestalPrimary}) no-repeat`}}/>
      <div className={styles.pedestalSecondary}
           style={{background: `linear-gradient(${art.secondary}, ${art.secondary}), url(${pedestalSecondary}) no-repeat`}}/>
      <div className={styles.pedestalShadow}
           style={{background: `linear-gradient(${art.shadow}, ${art.shadow}), url(${pedestalShadow}) no-repeat`}}/>
      <img src={windowBackground} className={styles.windowBackground} alt=""/>
      <div style={{display: checkTokens()}}>
        <img src={baseAttack} className={styles.attackToken} alt=""/>
        <p className={styles.baseAttack}>{props.baseAttack}</p>
        <img src={baseHp} className={styles.hpToken} alt=""/>
        <p className={styles.baseHp}>{props.baseHp}</p>
      </div>
      <div className={styles.heroAndShadow}>
        <img src={art.sprite.named} className={styles.hero} alt="hero"/>
        <img src={art.sprite.shadow} className={styles.heroShadow} alt=""/>
      </div>
    </div>
  )
}

export default CardDisplay