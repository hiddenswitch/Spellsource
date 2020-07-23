import React from 'react'
import styles from './card-display.module.css'

import backgroundLayer from '../card-images/layer-1.png'
import whiteBanner from '../card-images/layer-3.png'
import highlight from '../card-images/layer-4.png'
import shadow from '../card-images/layer-5.png'
import secondary from '../card-images/layer-6.png'
import baseAttack from '../card-images/attack-token.png'
import baseHp from '../card-images/health-token.png'

import pedestalPrimary from '../card-images/pedestal-primary.png'
import pedestalSecondary from '../card-images/pedestal-secondary.png'
import pedestalShadow from '../card-images/pedestal-shadow.png'
import windowBackground from '../card-images/large-card-window-background.png'
import selkie from '../card-images/selkie.png'
import selkieShadow from '../card-images/selkie-shadow.png'
import { defaultsDeep} from 'lodash'

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
  }
}

const toRgbaString = (v) => {
  return `rgba(${v.r * 255}, ${v.g * 255}, ${v.b * 255}, ${v.a})`
}

function CardDisplay (props) {
  let art = {}
  defaultsDeep(art, props.art, defaultArt)
  art = {
    ...art,
    body: {
      ...art.body,
      vertex: toRgbaString(art.body.vertex)
    },
    highlight: toRgbaString(art.highlight),
    primary: toRgbaString(art.primary),
    secondary: toRgbaString(art.secondary),
    shadow: toRgbaString(art.shadow),
  }

  return (
    <div className={styles.cardDisplayTemplate}>
      <img src={backgroundLayer} className={styles.layerOne} alt='card'/>
      <div className={styles.descriptionBox}>
        <p className={styles.description} style={{
          color: art.body.vertex
        }}>{props.description}</p>
        <p className={styles.type}>{props.type}</p>
      </div>
      <p className={styles.baseManaCost}>{props.baseManaCost}</p>
      <p className={styles.name}
         style={{
           color: art.body.vertex
         }}>{props.name}</p>
      <div className={styles.primary}
           style={{ background: `linear-gradient(${art.primary}, ${art.primary}), url(${whiteBanner}) no-repeat` }}/>
      <div className={styles.highlight}
           style={{ background: `linear-gradient(${art.highlight}, ${art.highlight}), url(${highlight}) no-repeat` }}/>
      <div className={styles.shadow}
           style={{ background: `linear-gradient(${art.shadow}, ${art.shadow}), url(${shadow}) no-repeat` }}/>
      <div className={styles.secondary}
           style={{ background: `linear-gradient(${art.secondary}, ${art.secondary}), url(${secondary}) no-repeat` }}/>
      <div className={styles.pedestalPrimary}
           style={{ background: `linear-gradient(${art.primary}, ${art.primary}), url(${pedestalPrimary}) no-repeat` }}/>
      <div className={styles.pedestalSecondary}
           style={{ background: `linear-gradient(${art.secondary}, ${art.secondary}), url(${pedestalSecondary}) no-repeat` }}/>
      <div className={styles.pedestalShadow}
           style={{ background: `linear-gradient(${art.shadow}, ${art.shadow}), url(${pedestalShadow}) no-repeat` }}/>
      <img src={windowBackground} className={styles.windowBackground} alt=""/>
      <img src={baseAttack} className={styles.attackToken} alt=""/>
      <p className={styles.baseAttack}>{props.baseAttack}</p>
      <img src={baseHp} className={styles.hpToken} alt=""/>
      <p className={styles.baseHp}>{props.baseHp}</p>
      <div className={styles.heroAndShadow}>
        <img src={selkie} className={styles.hero} alt="hero"/>
        <img src={selkieShadow} className={styles.heroShadow} alt=""/>
      </div>
    </div>
  )
}

export default CardDisplay