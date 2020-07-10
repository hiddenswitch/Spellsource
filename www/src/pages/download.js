import React from 'react'
import { AnchorLink } from "gatsby-plugin-anchor-links"
import Layout from '../components/creative-layout'
import styles from '../components/creative-layout.module.scss'

import android from '../assets/android-logo.png'
import iOS from '../assets/iOS-logo.png'
import firefox from '../assets/firefox-logo.png'
import chrome from '../assets/chrome-logo.png'
import playNow from '../assets/play-now.gif'

const Download = () => {
  return (
    <Layout>
      <h2>Play Now</h2>
      <section style={{ textAlign: 'center' }}>
        <figure>
          <AnchorLink to="/download#ios-users"><img src={iOS} alt="iOS" className={styles.smallLogo}  /></AnchorLink>
          <figcaption>iPhone</figcaption>
        </figure>

        <figure>
          <AnchorLink to="/download#android-users"><img src={android} alt="Android" className={styles.smallLogo}  /></AnchorLink>
          <figcaption>Android</figcaption>
        </figure>

        <figure>
          <a href="/game"><img src={firefox} alt="Firefox" className={styles.smallLogo}  /></a>
          <figcaption>Firefox</figcaption>
        </figure>

        <figure>
          <a href="/game"><img src={chrome} alt="Chrome" className={styles.smallLogo}/></a>
          <figcaption>Chrome</figcaption>
        </figure>
      </section>
      <p>For users on other browsers, go to our <a href="https://discord.gg/HmbESh2">Discord</a> and ask for the Steam key.</p>
      <section>
        <h2>Mobile Betas</h2>
        <h4 id="ios-users">iOS Users</h4>
        <p>You can find the iOS Beta <a href="https://testflight.apple.com/join/pkMfO2qa">here</a>. You will be prompted to join our TestFlight group, which is Apple's testing service. You will remain anonymous when you join Testflight this way.</p>
        <h4 id="android-users">Android Users</h4>
        <p>Join the <a href="https://groups.google.com/forum/#!forum/spellsource-alpha-testers">Spellsource Google Group</a>, then follow the instructions in the welcome message to participate in testing.
          You must be logged into Google Play to join the testing program. Your e-mail will be shared with the developers.
        </p>
        <br/>
        <div style={{ textAlign: 'center' }}>
          <img src={playNow} className={styles.emphasisGif} alt="in-game gif"/>
        </div>
      </section>
    </Layout>
  )
}

export default Download