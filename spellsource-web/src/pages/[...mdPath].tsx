import { GetStaticPaths, GetStaticPropsContext, InferGetStaticPropsType } from "next";
import React from "react";
import path from "path";
import * as glob from "glob-promise";
import PublicSiteLayout, { ContentPanel, PublicPageHeader } from "../components/public-site-layout";
import { getHtmlFromMd, useReactForHtml } from "../lib/markdown";
import * as styles from "../components/public-site-layout.module.scss";

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

export const getStaticProps = async ({ params }: GetStaticPropsContext) => ({
  props: {
    data: await getHtmlFromMd(directory, `${path.join(...params!.mdPath!)}.md`),
  },
});

export default ({ data }: InferGetStaticPropsType<typeof getStaticProps>) => {
  const content = useReactForHtml(data?.contentHtml);
  const metadata = data as typeof data & { title?: string; layout?: string; path?: string };
  const isWikiHub = metadata?.layout === "wiki" && metadata?.path === "/wiki";
  const isWikiPage = metadata?.layout === "wiki";
  const isUpdatesPage = metadata?.path === "/whats-new";
  const isCreditsPage = metadata?.path === "/credits";
  const title = metadata?.title ?? "Spellsource";
  const eyebrow = isWikiPage ? "World guide" : isUpdatesPage ? "Release notes" : isCreditsPage ? "Acknowledgements" : "Spellsource";

  return (
    <PublicSiteLayout title={title}>
      <PublicPageHeader eyebrow={eyebrow} title={title} />
      <ContentPanel variant="document" className={`${isWikiHub ? styles.wikiHubPanel : ""} markdown`}>
        {content}
      </ContentPanel>
    </PublicSiteLayout>
  );
};
