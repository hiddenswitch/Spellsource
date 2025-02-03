import { NextApiRequest, NextApiResponse } from "next";
import path from "path";
import fs from "fs";
import matter from "gray-matter";
import { remark } from "remark";
import { Plugin } from "unified";
import { Node, Parent } from "unist";
import { Heading, Link, Text } from "mdast";

const whatsNewFile = path.join(process.cwd(), "src", "pages-markdown", `whats-new.md`);

const remarkTextMeshPro: Plugin = () => (tree: Node, file: any) => {
  file.result = processNode(tree);
};

const depthToPercent = (depth: number) => {
  switch (depth) {
    case 1:
      return "200%";
    case 2:
      return "150%";
    case 3:
      return "125%";
    case 4:
      return "110%";
    default:
      return "100%";
  }
};

function processNode(node: Node): string {
  const content = "children" in node ? (node as Parent).children.map(processNode).join("") : "";

  switch (node.type) {
    case "root":
      return content.trim();
    case "paragraph":
      return `${content}\n`;
    case "strong":
      return `<b>${content}</b>`;
    case "emphasis":
      return `<i>${content}</i>`;
    case "link":
      const url = (node as Link).url;
      return `<color=#40d2f0><u><link="${url}">${content}</link></u></color>`;
    case "inlineCode":
    case "text":
      return (node as Text).value;
    case "heading":
      return `\n<b><size=${depthToPercent((node as Heading).depth)}>${content}</size></b>\n`;
    case "listItem":
      return ` - ${content}`;
    case "list":
    default:
      return content;
  }
}

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const fileContents = await fs.promises.readFile(whatsNewFile, { encoding: "utf8" });
  const matterResult = matter(fileContents);
  const processedContent = await remark().use(remarkTextMeshPro).process(matterResult.content);

  res.setHeader("Content-Type", "text/plain");
  res.send(processedContent.result);
  res.end();
}
