import React from "react";
import * as styles from './creative-layout.module.scss'
import { Link } from 'gatsby'

function Footer({pages}) {
  return <div className={styles.footer}>
    <strong>2022 HiddenSwitch, Inc</strong>
    <ul>
        <li><Link to="/download">About</Link></li>
        <li><Link to="/download">Developer API</Link></li>
        <li><Link to="/download">Disacord</Link></li>
        <li><Link to="/download">Careers</Link></li>
        {pages}
        <li><Link to="/download">Contributors</Link></li>
        <li><Link to="/download">HiddenSwitch</Link></li>
    </ul>
  </div>;
}

export default Footer;
