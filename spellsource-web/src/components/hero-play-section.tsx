import React from 'react'
import * as styles from './creative-layout.module.scss'
import playNow from '../../public/static/assets/play-now.gif'
import cloud from '../../public/static/assets/transparent-cloud.png'

function HeroPlaySection() {
  return (
    <div className={`${styles.heroContainerAlt} ${styles.backgroundGradient2}`}>
      <img src={cloud} style={{ top: '-10%', left: '8%' }} className={`${styles.cloud} ${styles.lcloud}`}/>
      <img src={cloud} style={{ top: '0', right: '3%' }} className={`${styles.cloud} ${styles.mcloud}`}/>
      <img src={cloud} style={{ bottom: '1%', right: '43%' }} className={`${styles.cloud} ${styles.mcloud}`}/>
      <img src={cloud} style={{ bottom: '3%', right: '-5%' }} className={`${styles.cloud} ${styles.scloud}`}/>
      <img src={cloud} style={{ top: '50%', left: '-11%' }} className={`${styles.cloud} ${styles.scloud}`}/>

      <div className={styles.heroPlayContainer}>
      <h1 className={styles.heroSectionTitle}>Mobile Beta</h1>
      <h3 className={styles.playSectionBody}>iOS Users</h3>
      <h4 className={styles.playSectionBody}>You can find the iOS Beta <a href="https://testflight.apple.com/join/pkMfO2qa">here</a>. You will be prompted to join our TestFlight group, which is Apple's testing service. You will remain anonymous when you join Testflight this way.</h4>
      <h3 className={styles.playSectionBody}>Android Users</h3>
      <h4 className={styles.playSectionBody}>Join the <a href="https://groups.google.com/forum/#!forum/spellsource-alpha-testers">Spellsource Google Group</a>, then follow the instructions in the welcome message to participate in testing.
        You must be logged into Google Play to join the testing program. Your e-mail will be shared with the developers.
      </h4>
      <h3 className={styles.playSectionBody}>PC Users</h3>
      <h4 className={styles.playSectionBody}>For users on other browsers, go to our <a href="https://discord.gg/HmbESh2">Discord</a> and ask for the Steam key.</h4>
      </div>
      <div className={styles.heroImageContainer}>
        <img src={playNow} className={styles.heroImage}/>
      </div>
    </div>
  )
}

export default HeroPlaySection