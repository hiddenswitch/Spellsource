import Link from "next/link";
import React from "react";

const PostLink = ({ post }) => <Link href={post.frontmatter.path}>{post.frontmatter.title}</Link>;

export default PostLink;