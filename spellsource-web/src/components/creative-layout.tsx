import React from "react"
import Header from "./header"
import Footer from "./footer"
import * as styles from "./creative-layout.module.scss"
import Link from "next/link"
import { signIn, signOut } from "next-auth/react"
import { AuthButton } from "./auth-button"
import Head from "next/head"

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
]

export default ({ children }) => {
  return (
    <div className={styles.container}>
      <Head>
        <title>Spellsource</title>
      </Head>
      <Header pages={pages} />
      {children}
      <Footer pages={pages} />
    </div>
  )
}
