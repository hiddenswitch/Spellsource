import { GetStaticPaths, GetStaticPropsContext, InferGetStaticPropsType } from "next"
import React from "react"
import path from "path"
import * as glob from "glob-promise"
import Layout from "../components/creative-layout"
import * as styles from "../templates/template-styles.module.scss"
import { Container } from "react-bootstrap"
import cx from "classnames"
import { getHtmlFromMd, useReactForHtml } from "../lib/markdown"

const directory = path.join(process.cwd(), "src", "pages-markdown")

export const getStaticPaths: GetStaticPaths = async () => {
  const fullPath = path.join(directory, "**/*.md")
  const files = await glob.promise(fullPath)

  const paths = files.map((fileName) => {
    const mdPath = path.relative(directory, fileName).replace(/\.md$/, "").split(path.sep)

    return {
      params: {
        mdPath,
      },
    }
  })

  return {
    paths,
    fallback: false,
  }
}

export const getStaticProps = async ({ params }: GetStaticPropsContext) => ({
  props: {
    data: await getHtmlFromMd(directory, `${path.join(...params.mdPath)}.md`),
  },
})

export default ({ data }: InferGetStaticPropsType<typeof getStaticProps>) => {
  const content = useReactForHtml(data?.contentHtml)

  return (
    <Layout>
      <Container className={cx(styles.templateContainer, "markdown")}>{content}</Container>
    </Layout>
  )
}
