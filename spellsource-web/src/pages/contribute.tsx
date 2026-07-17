import { InferGetStaticPropsType } from "next";
import React from "react";
import { getHtmlFromMd, useReactForHtml } from "../lib/markdown";
import PublicSiteLayout, { ContentPanel, PublicPageHeader } from "../components/public-site-layout";
import path from "path";

export const getStaticProps = async () => ({
  props: {
    data: await getHtmlFromMd(path.join(process.cwd(), ".."), "CONTRIBUTE.md"),
  },
});

export default ({ data }: InferGetStaticPropsType<typeof getStaticProps>) => {
  const content = useReactForHtml(data?.contentHtml);

  return (
    <PublicSiteLayout title="Contribute to Spellsource">
      <PublicPageHeader eyebrow="Open source" title="Contribute" />
      <ContentPanel variant="document" className="markdown">
        {content}
      </ContentPanel>
    </PublicSiteLayout>
  );
};
