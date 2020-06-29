import React from 'react'
import { Link } from 'gatsby'
import Layout from '../components/creative-layout'
import {Parallax, ParallaxLayer} from 'react-spring/renderprops-addons'
import styles from '../components/creative-layout.module.scss'

import spellsource from '../assets/spellsource-resized.png'
import gameEnvironment from '../assets/sector-5.png'
import cloud from '../assets/transparent-cloud.png'
import champions from '../assets/champions-without-names.png'

const url = (name, wrap = false) => `${wrap ? 'url(' : ''}https://awv3node-homepage.surge.sh/build/assets/${name}.svg${wrap ? ')' : ''}`

const Index = () => {
  let parallax
  return (
    <Layout>
      <section className={styles.fullscreenDiv}>
        <Parallax pages={2} scrolling={true} vertical ref={ref => (parallax = ref)}>
          <ParallaxLayer offset={0} speed={1} style={{ backgroundImage: 'linear-gradient(black, #533B63, #341F42)' }} />
          <ParallaxLayer offset={0.99} speed={1} style={{ backgroundImage: `url(${gameEnvironment})`, backgroundSize: 'cover' }} />

          <ParallaxLayer offset={0} speed={0} factor={3} style={{ backgroundImage: url('stars', true), backgroundSize: 'cover' }}
                         onClick={() => parallax.scrollTo(1)} />

          <ParallaxLayer offset={0.43} speed={0.8} style={{ opacity: 0.1 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '55%' }} />
            <img src={cloud} style={{ display: 'block', width: '10%', marginLeft: '15%' }} />
          </ParallaxLayer>

          <ParallaxLayer offset={0.58} speed={0.5} style={{ opacity: 0.1 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '70%' }} />
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '40%' }} />
          </ParallaxLayer>

          <ParallaxLayer offset={0.33} speed={0.2} style={{ opacity: 0.2 }}>
            <img src={cloud} style={{ display: 'block', width: '10%', marginLeft: '10%' }} />
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '75%' }} />
          </ParallaxLayer>

          <ParallaxLayer offset={0.53} speed={0.4} style={{ opacity: 0.4 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '60%' }} />
            <img src={cloud} style={{ display: 'block', width: '25%', marginLeft: '30%' }} />
            <img src={cloud} style={{ display: 'block', width: '10%', marginLeft: '80%' }} />
          </ParallaxLayer>

          <ParallaxLayer offset={0.865} speed={1.1} style={{ opacity: 0.6 }}>
            <img src={cloud} style={{ display: 'block', width: '20%', marginLeft: '5%' }} />
            <img src={cloud} style={{ display: 'block', width: '15%', marginLeft: '75%' }} />
          </ParallaxLayer>

          <ParallaxLayer
            offset={0}
            // speed={-0.9}
            speed={1}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src={spellsource} alt="In-Game Screenshot" style={{ width: '50%' }} />
          </ParallaxLayer>

          <ParallaxLayer
            offset={0.8}
            speed={0}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src={champions} alt="Champions"style={{ width: '50%' }}/>
          </ParallaxLayer>

          <ParallaxLayer
            offset={1.5}
            speed={0.5}>
            <section>
              <h3 style={{textAlign: 'center'}}>
                <Link to="/download">[Play Now]</Link> - <a href="https://discord.gg/HmbESh2">[Discord]</a> - <Link to="/contribute">[Contribute]</Link>
              </h3>
              <p></p>
              <p>Spellsource is a community-driven digital card game <b>where every card is free</b>. Spellsource features
                thousands of community-authored cards. </p>
              <p>Learn more about the game <Link to="../wiki/Main_Page">here</Link> and the keywords <Link
                  to="/keywords">here</Link>. </p>
              <p>Support us by <Link to="/contribute">authoring your own cards</Link> and <a
                href="mailto:ben@hiddenswitch.com">contact us</a> about your own art.</p>
              <p>This is alpha-quality software. You can see more on our <a
                href="https://github.com/hiddenswitch/Spellsource-Server">GitHub</a>. </p>
            </section>
          </ParallaxLayer>
        </Parallax>
      </section>
    </Layout>
)
}

export default Index