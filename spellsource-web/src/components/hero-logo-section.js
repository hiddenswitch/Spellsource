import React from 'react'
import styles from './creative-layout.module.scss'
import spellsourceLogo from '../assets/spellsource.png'
import cloud from '../assets/transparent-cloud.png'

function HeroLogoSection() {
  return (
    <div className={`${styles.heroSpellsourceLogo}`} >
      <img src={spellsourceLogo} className={styles.spellsourceLogo}/>
      <img src={cloud} style={{ top: '10%', left: '5%' }} className={`${styles.cloud} ${styles.mcloud}`}/>
      <img src={cloud} style={{ bottom: '10%', left: '10%' }} className={`${styles.cloud} ${styles.lcloud}`}/>
      <img src={cloud} style={{ top: '15%', right: '3%' }} className={`${styles.cloud} ${styles.mcloud}`}/>
      <img src={cloud} style={{ bottom: '-5%', right: '8%' }} className={`${styles.cloud} ${styles.scloud}`}/>
    </div>
  )
}

export default HeroLogoSection