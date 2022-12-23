import React from 'react'
import * as styles from './creative-layout.module.scss'
import spellsourceLogo from '../assets/spellsource.png'

function HeroLogoSection({ref}) {
  return (
    <div className={`${styles.heroContainer} ${styles.heroSpellsourceLogo}`} ref={ref}>
      <img src={spellsourceLogo} className={styles.spellsourceLogo}/>
    </div>
  )
}

export default HeroLogoSection