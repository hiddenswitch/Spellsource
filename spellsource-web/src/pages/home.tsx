import Layout from "../components/creative-layout";
import { Card, Col, Container, Row } from "react-bootstrap";
import React, { FunctionComponent } from "react";
import cx from "classnames";
import Link from "next/link";
import Image, { StaticImageData } from "next/image";
import gameplay from "public/static/assets/gameplay.png";
import cardEditor from "public/static/assets/card-editor.png";
import collection from "public/static/assets/collection.png";
import map from "public/static/wiki/Artboard_1-50.jpg";

const Rectangle: FunctionComponent<{
  href: string;
  image: StaticImageData;
  label: string;
}> = ({ href, image, label }) => {
  return (
    <Col>
      <Link href={href} aria-label={label}>
        <Card className={"hover-zoom shadow-lg"}>
          <div className={"overflow-hidden d-inline-block d-flex justify-content-center"}>
            <Image quality={100} alt={""} height={"400"} src={image} className={"rounded-top"} />
          </div>
          <h4 className={cx("p-3", "text-center")}>{label}</h4>
        </Card>
      </Link>
    </Col>
  );
};

export default () => {
  return (
    <Layout className={"overflow-hidden"}>
      <div
        className={"position-absolute top-50 start-50"}
        style={{
          backgroundImage: `url("/static/assets/sector-5.png")`,
          backgroundPosition: "center",
          backgroundSize: "cover",
          filter: "blur(5px)",
          height: "110%",
          width: "110%",
          transform: "translate(-50%, -50%)",
        }}
      />
      <Container className={cx("flex-grow-1", "d-flex", "align-items-center")}>
        <Row className={cx("row-cols-1", "row-cols-md-2", "row-cols-lg-3", "row-cols-xl-4", "py-5", "g-5")}>
          <Rectangle href={"/download"} image={gameplay} label={"Download"}></Rectangle>
          <Rectangle href={"/card-editor"} image={cardEditor} label={"Create Cards"}></Rectangle>
          <Rectangle href={"/collection"} image={collection} label={"Collection"}></Rectangle>
          <Rectangle href={"/wiki"} image={map} label={"Wiki"}></Rectangle>
        </Row>
      </Container>
    </Layout>
  );
};
