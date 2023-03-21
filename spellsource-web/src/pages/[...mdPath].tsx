import {GetStaticPaths, GetStaticPropsContext, InferGetStaticPropsType} from "next";
import React from "react";
import path from "path";
import * as glob from "glob-promise";
import {promisify} from "util";
import fs from "fs";
import {remark} from "remark";
import remarkHtml from "remark-html";
import matter from "gray-matter";
import Layout from '../components/creative-layout'
import * as styles from '../templates/template-styles.module.scss'

const directory = path.join(process.cwd(), "src", "pages-markdown");

export const getStaticPaths: GetStaticPaths = async () => {
  const fullPath = path.join(directory, "**/*.md");
  const files = await glob.promise(fullPath);

  const paths = files.map((fileName) => {
    const mdPath = path.relative(directory, fileName).replace(/\.md$/, "").split(path.sep);
    return {
      params: {
        mdPath,
      },
    };
  });

  return {
    paths,
    fallback: false,
  };
};

export const getStaticProps = async ({params}: GetStaticPropsContext) => {
  const fullPath = path.join(directory, `${path.join(...params.mdPath)}.md`);
  const fileContents = await fs.promises.readFile(fullPath, {encoding: "utf8"});
  const matterResult = matter(fileContents);
  const processedContent = await remark().use(remarkHtml).process(matterResult.content);
  const contentHtml = processedContent.toString();

  return {
    props: {
      data: {
        contentHtml,
        ...matterResult.data,
      }
    }
  };
};

export default ({data}: InferGetStaticPropsType<typeof getStaticProps>) => {
  return (
    <Layout>
      <div className={styles.templateContainer} dangerouslySetInnerHTML={{__html: data?.contentHtml ?? ""}}/>
    </Layout>
  );
};