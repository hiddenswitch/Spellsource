import React from 'react'
import * as styles from './creative-layout.module.scss'
import playNow from '../assets/play-now.gif'

function HeroPlaySection() {
  const title = 'Mobile Beta';
  const iOSBody = 'You can find the iOS beta here, You will be prompted to join our TestFlight Group, which is Apple’s testing service. You can remain anonymous when you join TestFlight.'
  const androidBody = 'Join the Spellsource Google Group, then follow the instructions in the welcome message to participate in testing. You must be logged into Google Play to join the testing program. Your e-mail will be shared with the developers. '
  const pcBody = 'Go to our Discord and ask for the Steam Key.'

  return (
    <div className={`${styles.heroContainerAlt} ${styles.backgroundGradient2}`}>
      <div className={styles.heroTextContainer}>
        <SectionTitle title={title}/>
        <BodySectionTitle title='iOS Users'/>
        <BodySectionContent text={iOSBody}/>
        <BodySectionTitle title='Android Users'/>
        <BodySectionContent text={androidBody}/>
        <BodySectionTitle title='PC Users'/>
        <BodySectionContent text={pcBody}/>
      </div>
      <div className={styles.heroImageContainer}>
        <img src={playNow} className={styles.heroImage}/>
      </div>
    </div>
  )
}

const SectionTitle = ({title}) => {
  return <h1 className={styles.heroSectionTitle}>{title}</h1>
}
const BodySectionTitle = ({title}) => {
  return <h3 className={styles.playSectionTitle}>{title}</h3>
}

const BodySectionContent = ({text}) => {
  return <h3 className={styles.playSectionBody}>{text}</h3>
}

export default HeroPlaySection