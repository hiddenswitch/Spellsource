import Link from "next/link";
import React from "react";
import PublicSiteLayout, { ContentPanel, PublicPageHeader } from "../components/public-site-layout";

export default () => (
  <PublicSiteLayout title="Page not found">
    <PublicPageHeader eyebrow="Lost in the islands" title="Page not found" />
    <ContentPanel variant="document">
      <p>The requested page could not be found.</p>
      <p>
        If you reached this page by clicking on a link in the wiki, would you like to <Link href="/contribute">help us fill in</Link> the missing pages?
      </p>
      <p>
        <Link href={"/"}>Return to home.</Link>
      </p>
    </ContentPanel>
  </PublicSiteLayout>
);
