import React, {useEffect, useRef, useState,} from 'react'
import Layout from '../components/creative-layout'
import * as styles from '../components/creative-layout.module.scss';
import HeroIntroSection from '../components/hero-intro-section'
import HeroLogoSection from '../components/hero-logo-section'
import HeroPlaySection from '../components/hero-play-section'
import HeroGithub from '../components/hero-github'
import cloud from '../assets/transparent-cloud.png'

const Index = () => {
  const ref = useRef(null);
  const [w, h] = [window.innerWidth, window.innerHeight];
  
  return (
    <Layout>
      <Cloud x={`calc(${w}px/10)`} y={`calc(${h}px/5)`} size={'250px'} id='cloud2'/>
      <Cloud x={`calc(${w}px/6)`} y={`calc(${h}px/3 * 2.25)`} size={'250px'} id='cloud2'/>
      <HeroLogoSection ref={ref}/>
      <HeroIntroSection />
      <HeroPlaySection />
      <HeroGithub />
    </Layout>
  )
}

const Cloud = ({x, y, size, id}) => {
  return(
    <img id={id} src={cloud} style={{ position: 'absolute', overflow: 'hidden', width: size, top: y, left: x }} 
    className={styles.clouds} alt="cloud"/>
  )
}

export default Index
