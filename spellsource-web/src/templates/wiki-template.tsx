import React from "react";
import Layout from "../components/creative-layout";
import * as styles from "./template-styles.module.scss";

export default function Template({
  data, // this prop will be injected by the GraphQL query below.
}) {
  const { markdownRemark } = data; // data.markdownRemark holds your post data
  const { html } = markdownRemark;
  return (
    <Layout>
      <div className={styles.templateContainer}>
        <h2>{data.markdownRemark.frontmatter.title}</h2>
        <div dangerouslySetInnerHTML={{ __html: html }} />
      </div>
    </Layout>
  );
}

/*
export const pageQuery = graphql`
  query($path: String!) {
    markdownRemark(frontmatter: { path: { eq: $path } }) {
      html
      frontmatter {
        path
        title
      }
    }
  }
`*/
