import React from 'react'
import { Link } from 'gatsby'
import Layout from '../components/creative-layout'
import { Parallax, ParallaxLayer } from '@react-spring/parallax'
import * as styles from '../components/creative-layout.module.scss'

import spellsource from '../assets/spellsource-resized.png'
import gameEnvironment from '../assets/sector-5.png'
import cloud from '../assets/transparent-cloud.png'
import playingGif from '../assets/index-gif.gif'

const url = (name, wrap = false) => `${wrap ? 'url(' : ''}https://awv3node-homepage.surge.sh/build/assets/${name}.svg${wrap ? ')' : ''}`

const Index = () => {
  let parallax

  const parallaxPages = () => {
    if (typeof window !== 'undefined' && window.matchMedia('(max-width: 500px)').matches) {
      return (2.15)
    } else {
      return (1.99)
    }
  }

  return (
    <Layout>
      <section className={styles.fullscreenDiv}>
        <Parallax pages={parallaxPages()} scrolling={true} vertical ref={ref => (parallax = ref)}>
          <ParallaxLayer offset={0} speed={1}
                         style={{ backgroundImage: 'linear-gradient(#21215c, rgb(89,153,215))' }}/>

          <ParallaxLayer offset={0.99} speed={1} style={{
            backgroundImage: `url(${gameEnvironment})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center center'
          }}/>

          <ParallaxLayer offset={0} speed={0} factor={1.5}
                         style={{ backgroundImage: url('stars', true), backgroundSize: 'cover' }}/>

          <ParallaxLayer offset={0.43} speed={0.8} style={{ opacity: 0.1 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '55%' }} alt=""/>
            <img src={cloud} style={{ display: 'block', width: '10%', marginLeft: '15%' }} alt=""/>
          </ParallaxLayer>

          <ParallaxLayer offset={0.58} speed={0.5} style={{ opacity: 0.1 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '70%' }} alt=""/>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '40%' }} alt=""/>
          </ParallaxLayer>

          <ParallaxLayer offset={0.33} speed={0.2} style={{ opacity: 0.2 }}>
            <img src={cloud} style={{ display: 'block', width: '10%', marginLeft: '10%' }} alt=""/>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '75%' }} alt=""/>
          </ParallaxLayer>

          <ParallaxLayer offset={0.53} speed={0.4} style={{ opacity: 0.4 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '60%' }} alt=""/>
            <img src={cloud} style={{ display: 'block', width: '25%', marginLeft: '30%' }} alt=""/>
            <img src={cloud} style={{ display: 'block', width: '10%', marginLeft: '80%' }} alt=""/>
          </ParallaxLayer>

          <ParallaxLayer offset={0.865} speed={1.1} style={{ opacity: 0.6 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '5%' }} alt=""/>
            <img src={cloud} style={{ display: 'block', width: '15%', marginLeft: '75%' }} alt=""/>
          </ParallaxLayer>

          <ParallaxLayer
            offset={0}
            speed={1}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}
            onClick={() => parallax.scrollTo(1.5)}>
            <img src={spellsource} alt="In-Game Screenshot" style={{ width: '700px', maxWidth: '95%' }}/>
          </ParallaxLayer>

          <ParallaxLayer offset={1} speed={1.1}>
            <section style={{ width: '700px', maxWidth: '95%', minHeight: '257.63px' }}>
              <h3 style={{ textAlign: 'center' }}>
                <Link to="/download">[Play Now]</Link> - <a href="https://discord.gg/HmbESh2">[Discord]</a> - <Link
                to="/contribute">[Contribute]</Link>
              </h3>
              <p></p>
              <p>Spellsource is a community-driven digital card game <b>where every card is free</b>. Spellsource
                features thousands of community-authored cards. </p>
              <p>Learn more about the game <Link to="/wiki/Main_Page">here</Link> and the keywords <Link
                to="/keywords">here</Link>. </p>
              <p>Support us by <Link to="/contribute">authoring your own cards</Link> and <a
                href="mailto:ben@hiddenswitch.com">contact us</a> about your own art.</p>
              <p>This is alpha-quality software. You can see more on our <a
                href="https://github.com/hiddenswitch/Spellsource-Server">GitHub</a>. </p>
            </section>
            <section style={{ textAlign: 'center', minHeight: '400px', height: 'auto' }}>
              <img src={playingGif} className={styles.emphasisGif} alt="in-game gif"/>
            </section>
          </ParallaxLayer>
        </Parallax>
      </section>
    </Layout>
  )
}

export default Index
