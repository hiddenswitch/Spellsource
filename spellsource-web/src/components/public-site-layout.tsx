import Head from "next/head";
import React, { PropsWithChildren, ReactNode } from "react";
import Header from "./header";
import Footer from "./footer";
import { pages } from "./creative-layout";
import * as styles from "./public-site-layout.module.scss";

export const PublicHero = ({ eyebrow, title, children, image }: { eyebrow: string; title: string; children?: ReactNode; image?: string }) => (
  <section className={styles.hero} style={image ? { backgroundImage: `linear-gradient(102deg, rgba(40,55,255,.9), rgba(122,104,222,.64) 50%, rgba(235,143,180,.46) 74%, rgba(255,190,135,.5)), url(${image})` } : undefined}>
    <div className={styles.shell}>
      <div className={styles.heroContent}>
        <span className={styles.heroEyebrow}>{eyebrow}</span>
        <h1>{title}</h1>
        {children}
      </div>
    </div>
  </section>
);

export const PublicPageHeader = ({ eyebrow, title, children, image = "/static/assets/sector-5.png" }: { eyebrow?: string; title: string; children?: ReactNode; image?: string }) => (
  <section className={styles.pageHeader} style={{ backgroundImage: `linear-gradient(98deg, rgba(40,55,255,.82), rgba(106,91,190,.56) 52%, rgba(239,166,191,.34)), url(${image})` }}>
    <div className={styles.shell}>
      <span>{eyebrow}</span>
      <h1>{title}</h1>
      {children}
    </div>
  </section>
);

export const ContentPanel = ({ children, className = "", variant = "default" }: PropsWithChildren<{ className?: string; variant?: "default" | "document" }>) => <section className={`${styles.panel} ${variant === "document" ? styles.documentPanel : ""} ${className}`}>{children}</section>;

export const EmptyState = ({ title, children }: PropsWithChildren<{ title: string }>) => (
  <div className={styles.emptyState}>
    <h2>{title}</h2>
    <p>{children}</p>
  </div>
);

export default function PublicSiteLayout({ children, title = "Spellsource" }: PropsWithChildren<{ title?: string }>) {
  return (
    <div className={`${styles.site} public-site`}>
      <Head>
        <title>{title}</title>
      </Head>
      <Header pages={pages} />
      <main className="flex-grow-1">{children}</main>
      <Footer pages={pages} />
    </div>
  );
}
