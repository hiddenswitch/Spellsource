import Link from "next/link";
import React from "react";
import * as styles from "./creative-layout.module.scss";
import { Container } from "react-bootstrap";

function Footer({ pages }: { pages?: any }) {
  return (
    <div className={styles.navbarFooter}>
      <Container className={styles.footer}>
        <a href="https://hiddenswitch.com/" style={{ textDecoration: "none" }}>
          2022 HiddenSwitch, Inc
        </a>
        <span className={"d-flex gap-4"}>
          <a href="/javadoc">Developer API</a>
          <Link href="https://discord.gg/HmbESh2">Discord</Link>
          <Link href="/contribute">Contribute</Link>
        </span>
      </Container>
    </div>
  );
}

export default Footer;
