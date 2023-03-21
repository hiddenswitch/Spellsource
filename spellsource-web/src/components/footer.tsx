import Link from "next/link";
import React from "react";
import * as styles from './creative-layout.module.scss'

function Footer({pages}) {
  return <div className={styles.footer}>
    <a href="https://hiddenswitch.com/" style={{textDecoration:  'none'}}> 
      2022 HiddenSwitch, Inc
    </a>
    <ul>
      <li key={'javadocs'}><a href="/javadoc">Developer API</a></li>
      <li><Link href="https://discord.gg/HmbESh2">Discord</Link></li>
      <li><Link href="/contribute">Contribute</Link></li>
    </ul>
  </div>;
}

export default Footer;
