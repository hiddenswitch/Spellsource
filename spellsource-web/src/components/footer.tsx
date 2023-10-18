import Link from "next/link";
import React from "react";
import * as styles from "./creative-layout.module.scss";
import { Col, Container, Row } from "react-bootstrap";
import cx from "classnames";

function Footer({ pages }: { pages?: any }) {
  return (
    <div className={styles.navbarFooter}>
      <Container className={styles.footer}>
        <Row className={cx("justify-content-around", "w-100")}>
          <Col xs={"auto"}>
            <a href="https://hiddenswitch.com/">2023 HiddenSwitch, Inc</a>
          </Col>
          <Col xs={"auto"}>
            <a href="/javadoc">Developer API</a>
          </Col>
          <Col xs={"auto"}>
            <Link href="https://discord.gg/HmbESh2">Discord</Link>
          </Col>
          <Col xs={"auto"}>
            <Link href="/contribute">Contribute</Link>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default Footer;
