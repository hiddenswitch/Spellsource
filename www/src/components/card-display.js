import React, { useRef, useState } from 'react'
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

function CardDisplay (props) {
  const checkIfNull = (field) => {
    if (props.art) {
      if (field === 'text' && props.art.body && props.art.body.vertex) {
        return (`rgba(${props.art.body.vertex.r * 255},${props.art.body.vertex.g * 255},${props.art.body.vertex.b * 255},${props.art.body.vertex.a * 255})`)
      } else if (field === 'highlightColor' && props.art.highlight) {
        return (`rgba(${props.art.highlight.r * 255}, ${props.art.highlight.g * 255}, ${props.art.highlight.b * 255}, ${props.art.highlight.a * 255})`)
      } else if (field === 'primaryColor' && props.art.primary) {
        return (`rgba(${props.art.primary.r * 255}, ${props.art.primary.g * 255}, ${props.art.primary.b * 255}, ${props.art.primary.a * 255})`)
      } else if (field === 'secondaryColor' && props.art.secondary) {
        return (`rgba(${props.art.secondary.r * 255}, ${props.art.secondary.g * 255}, ${props.art.secondary.b * 255}, ${props.art.secondary.a * 255})`)
      } else if (field === 'shadowColor' && props.art.shadow) {
        return (`rgba(${props.art.shadow.r * 255}, ${props.art.shadow.g * 255}, ${props.art.shadow.b * 255}, ${props.art.shadow.a * 255})`)
      }
    } else {
      if (field === 'text') {
        return (`rgba(255, 255, 255, 255)`)
      } else if (field === 'highlightColor') {
        return (`rgba(251, 165, 140, 255)`)
      } else if (field === 'primaryColor') {
        return (`rgba(250, 121, 83, 255)`)
      } else if (field === 'secondaryColor') {
        return (`rgba(216, 98, 62, 255)`)
      } else if (field === 'shadowColor') {
        return (`rgba(151, 61, 23, 255)`)
      }
    }
  }

  return (
    <div className={styles.cardDisplayTemplate}>
      <img src={backgroundLayer} className={styles.layerOne} alt='card'/>
      <div className={styles.descriptionBox}>
        <p className={styles.description} style={{
          color: checkIfNull('text')
        }}>{props.description}</p>
        <p className={styles.type}>{props.type}</p>
      </div>
      <p className={styles.baseManaCost}>{props.baseManaCost}</p>
      <p className={styles.name}
         style={{
           color: checkIfNull('text')
         }}>{props.name}</p>
      <div className={styles.primary}
           style={{ background: `linear-gradient(${checkIfNull('primaryColor')}, ${checkIfNull('primaryColor')}), url(${whiteBanner}) no-repeat` }}/>
      <div className={styles.highlight}
           style={{ background: `linear-gradient(${checkIfNull('highlightColor')}, ${checkIfNull('highlightColor')}), url(${highlight}) no-repeat` }}/>
      <div className={styles.shadow}
           style={{ background: `linear-gradient(${checkIfNull('shadowColor')}, ${checkIfNull('shadowColor')}), url(${shadow}) no-repeat` }}/>
      <div className={styles.secondary}
           style={{ background: `linear-gradient(${checkIfNull('secondaryColor')}, ${checkIfNull('secondaryColor')}), url(${secondary}) no-repeat` }}/>
      <div className={styles.pedestalPrimary}
           style={{ background: `linear-gradient(${checkIfNull('primaryColor')}, ${checkIfNull('primaryColor')}), url(${pedestalPrimary}) no-repeat` }}/>
      <div className={styles.pedestalSecondary}
           style={{ background: `linear-gradient(${checkIfNull('secondaryColor')}, ${checkIfNull('secondaryColor')}), url(${pedestalSecondary}) no-repeat` }}/>
      <div className={styles.pedestalShadow}
           style={{ background: `linear-gradient(${checkIfNull('shadowColor')}, ${checkIfNull('shadowColor')}), url(${pedestalShadow}) no-repeat` }}/>
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