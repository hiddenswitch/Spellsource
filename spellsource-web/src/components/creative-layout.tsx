import React, { CSSProperties, PropsWithChildren } from "react";
import Header from "./header";
import Footer from "./footer";
import * as styles from "./creative-layout.module.scss";
import Link from "next/link";
import { AuthButton } from "./auth-button";
import Head from "next/head";
import { use100vh } from "react-div-100vh";
import cx from "classnames";

export const pages = [
  <li key={0}>
    <Link href={"/collection"}>Collection</Link>
  </li>,
  <li key={1}>
    <Link href={"/card-editor"}>Editor</Link>
  </li>,
  <li key={2}>
    <Link href={"/wiki"}>Wiki</Link>
  </li>,
  <li key={3}>
    <Link href={"/whats-new"}>What's New</Link>
  </li>,
  <li key={4}>
    <Link href={"/credits"}>Credits</Link>
  </li>,
  <li key={5}>
    <AuthButton />
  </li>,
];

export default ({ children, className, style }: PropsWithChildren<{ className?: string; style?: CSSProperties }>) => {
  const height = use100vh() ?? 1000;

  return (
    <div className={styles.container} style={{ minHeight: height }}>
      <Head>
        <title>Spellsource</title>
      </Head>
      <Header pages={pages} />
      <div className={cx("flex-grow-1", "d-flex", "flex-column", "position-relative", className)} style={style}>
        {children}
      </div>
      <Footer pages={pages} />
    </div>
  );
};
