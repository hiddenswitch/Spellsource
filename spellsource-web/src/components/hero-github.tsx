import React from 'react'
import * as styles from './creative-layout.module.scss'
import githubLogo from '../assets/Github.png'

function HeroGithub() {
  return (
    <div className={`${styles.heroContainerGithub} ${styles.backgroundGradient3}`}>
      <div className={styles.githubTextContainer}>
        <SectionTitle title='Ready to help our project?'/>
        <SectionSubText title='Join the over 40 contributors helping maintain and bring this project to life'/>
        <a href={'https://github.com/hiddenswitch/Spellsource'}>
          <img src={githubLogo} style={{maxWidth: '96px', alignSelf: 'center', padding: '92px 0 20px 0'}} />
        </a>
        <h3>Check out Spellsource on Github</h3>
        </div>
    </div>
  )
}

const SectionTitle = ({title}) => {
  return <h1 className={styles.githubSectionTitle}>{title}</h1>
}
const SectionSubText = ({title}) => {
  return <h4 className={styles.githubSectionSubText}>{title}</h4>
}

export default HeroGithub