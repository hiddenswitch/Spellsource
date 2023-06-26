import { InferGetStaticPropsType } from "next";
import React from "react";
import { getHtmlFromMd, useReactForHtml } from "../lib/markdown";
import Layout from "../components/creative-layout";
import { Container } from "react-bootstrap";
import cx from "classnames";
import * as styles from "../templates/template-styles.module.scss";
import path from "path";

export const getStaticProps = async () => ({
  props: {
    data: await getHtmlFromMd(path.join(process.cwd(), ".."), "CONTRIBUTE.md"),
  },
});

export default ({ data }: InferGetStaticPropsType<typeof getStaticProps>) => {
  const content = useReactForHtml(data?.contentHtml);

  return (
    <Layout>
      <Container className={cx(styles.templateContainer, "markdown")}>{content}</Container>
    </Layout>
  );
};
