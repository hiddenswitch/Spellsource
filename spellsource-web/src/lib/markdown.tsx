import { remark } from "remark"
import remarkGfm from "remark-gfm"
import remarkRehype from "remark-rehype"
import rehypeRaw from "rehype-raw"
import rehypeStringify from "rehype-stringify"
import { rehype } from "rehype"
import rehypeParse from "rehype-parse"
import rehypeReact from "rehype-react"
import React, { createElement, Fragment, ReactNode, useMemo } from "react"
import Link from "next/link"
import cx from "classnames"
import path from "path"
import fs from "fs"
import matter from "gray-matter"

export const markdownToHtml = () =>
  remark().use(remarkGfm).use(remarkRehype, { allowDangerousHtml: true }).use(rehypeRaw).use(rehypeStringify)

export const htmlToReact = () =>
  rehype()
    .use(rehypeParse, { fragment: true })
    .use(rehypeReact, {
      createElement,
      Fragment,
      components: {
        a: ({ className, href, ...props }: any) =>
          href ? (
            <Link className={cx(className, "next-link")} href={href} {...props} />
          ) : (
            <a className={className} {...props} />
          ),
      },
    })

export const getHtmlFromMd = async (directory: string, file: string) => {
  const fullPath = path.join(directory, file)
  const fileContents = await fs.promises.readFile(fullPath, { encoding: "utf8" })
  const matterResult = matter(fileContents)
  const processedContent = await markdownToHtml().process(matterResult.content)
  const contentHtml = processedContent.toString()

  return {
    contentHtml,
    ...matterResult.data,
  }
}

export const useReactForHtml = (html: string | undefined): ReactNode => {
  return useMemo(() => htmlToReact().processSync(html).result, [html])
}
