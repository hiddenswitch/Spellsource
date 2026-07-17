import PublicSiteLayout, { PublicPageHeader } from "../components/public-site-layout";
import { Col, Row } from "react-bootstrap";
import React, { FunctionComponent } from "react";
import cx from "classnames";
import Link from "next/link";
import Image, { StaticImageData } from "next/image";
import gameplay from "../../public/static/assets/gameplay.png";
import cardEditor from "../../public/static/assets/card-editor.png";
import collection from "../../public/static/assets/collection.png";
import map from "../../public/static/game-ui/spellsource-world-map.png";
import { GetServerSideProps } from "next";
import { getSession } from "next-auth/react";
import * as styles from "../components/public-site-layout.module.scss";

export const getServerSideProps: GetServerSideProps = async (context) => ({
  props: { session: await getSession(context) },
});

const Rectangle: FunctionComponent<{
  href: string;
  image: StaticImageData;
  label: string;
}> = ({ href, image, label }) => {
  return (
    <Col>
      <Link href={href} aria-label={label} className={styles.hubCardLink}>
        <article className={styles.hubCard}>
          <div className={"overflow-hidden d-flex justify-content-center"}>
            <Image quality={100} alt={""} height={"400"} src={image} />
          </div>
          <h4>{label}</h4>
        </article>
      </Link>
    </Col>
  );
};

export default () => {
  return (
    <PublicSiteLayout title="Explore Spellsource">
      <PublicPageHeader eyebrow="Spellsource" title="Find your next route.">
        <p>Play, create, collect, and explore a community-built card game world.</p>
      </PublicPageHeader>
      <section className={`${styles.hubSection} ${styles.homeHub}`}>
        <Row className={cx("row-cols-1", "row-cols-md-2", "row-cols-lg-4", "g-4")}>
          <Rectangle href={"/download"} image={gameplay} label={"Download"}></Rectangle>
          <Rectangle href={"/card-editor"} image={cardEditor} label={"Create Cards"}></Rectangle>
          <Rectangle href={"/collection"} image={collection} label={"Collection"}></Rectangle>
          <Rectangle href={"/wiki"} image={map} label={"Wiki"}></Rectangle>
        </Row>
      </section>
    </PublicSiteLayout>
  );
};
