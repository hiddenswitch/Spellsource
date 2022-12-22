import React from 'react'
import * as styles from './creative-layout.module.scss'
import githubLogo from '../assets/Github.png'

function HeroGithub() {
  return (
    <div className={`${styles.heroContainerGithub} ${styles.backgroundGradient3}`}>
      <div className={styles.heroTextContainer}>
        <SectionTitle title='Ready to help our project?'/>
        <SectionSubText title='Join the over 40 contributors helping maintain and bring this project to life'/>
        <img src={githubLogo} style={{maxWidth: '96px', alignSelf: 'center', paddingTop: '92px'}} />
        <h3>Check out Spellsource on Github</h3>
        </div>
    </div>
  )
}

const SectionTitle = ({title}) => {
  return <h1 className={styles.githubSectionTitle}>{title}</h1>
}
const SectionSubText = ({title}) => {
  return <h3 className={styles.githubSectionSubText}>{title}</h3>
}

export default HeroGithub