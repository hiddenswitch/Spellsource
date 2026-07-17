import React from "react";
import PublicSiteLayout, { ContentPanel, PublicHero } from "../components/public-site-layout";

import android from "../../public/static/assets/android-logo.png";
import iOS from "../../public/static/assets/iOS-logo.png";
import playNow from "../../public/static/assets/play-now.gif";
import Link from "next/link";
import Image from "next/image";
import * as styles from "../components/public-site-layout.module.scss";

const Download = () => {
  return (
    <PublicSiteLayout title="Play Spellsource">
      <PublicHero eyebrow="Play Spellsource" title="Choose your next adventure." image="/static/assets/gradient-section-2.png">
        <p>Join the Spellsource beta on mobile, or find the community on Discord for other platforms.</p>
      </PublicHero>
      <ContentPanel variant="document">
        <h2>Mobile beta</h2>
        <section className={`${styles.platforms} text-center`}>
          <figure>
            <Link href="/download#ios-users">
              <Image src={iOS} alt="iOS" width={75} />
            </Link>
            <figcaption>iPhone</figcaption>
          </figure>

          <figure>
            <Link href="/download#android-users">
              <Image src={android} alt="Android" width={75} />
            </Link>
            <figcaption>Android</figcaption>
          </figure>

          {/*<figure>*/}
          {/*  <a href="/game"><img src={firefox} alt="Firefox" className={styles.smallLogo}  /></a>*/}
          {/*  <figcaption>Firefox</figcaption>*/}
          {/*</figure>*/}

          {/*<figure>*/}
          {/*  <a href="/game"><img src={chrome} alt="Chrome" className={styles.smallLogo}/></a>*/}
          {/*  <figcaption>Chrome</figcaption>*/}
          {/*</figure>*/}
        </section>
        <p>
          For players on other devices, go to our <a href="https://discord.gg/HmbESh2">Discord</a> and ask for the Steam key.
        </p>
        <section className="mt-4">
          <h4 id="ios-users">iOS Users</h4>
          <p>
            You can find the iOS Beta <a href="https://testflight.apple.com/join/pkMfO2qa">here</a>. You will be prompted to join our TestFlight group, which is Apple's testing service. You will remain anonymous when you join Testflight this way.
          </p>
          <h4 id="android-users">Android Users</h4>
          <p>
            Join the <a href="https://groups.google.com/forum/#!forum/spellsource-alpha-testers">Spellsource Google Group</a>, then follow the instructions in the welcome message to participate in testing. You must be logged into Google Play to join the testing program. Your e-mail will be shared with the developers.
          </p>
          <br />
          <div className="text-center">
            <Image src={playNow} className={styles.emphasisGif} alt="in-game gif" />
          </div>
        </section>
      </ContentPanel>
    </PublicSiteLayout>
  );
};

export default Download;
