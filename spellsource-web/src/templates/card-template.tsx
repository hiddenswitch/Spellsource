import React from "react";
import PublicSiteLayout, { ContentPanel, PublicPageHeader } from "../components/public-site-layout";
import CardDisplay, { CardDef } from "../components/collection/card-display";
import Link from "next/link";

const CardProperties = (props: { card: CardDef }) => {
  const { card } = props; // data.card holds your post data
  const { name, description, type, baseManaCost, baseAttack, baseHp, rarity, art, id } = card;
  let typeAndStats;
  if (type === "MINION") {
    typeAndStats = "(" + baseAttack + ", " + baseHp + ") " + type;
  } else {
    typeAndStats = type;
  }

  return (
    <>
      <h2>
        {name} ({baseManaCost})
      </h2>
      <p>{rarity}</p>
      <div>
        <CardDisplay name={name} baseManaCost={baseManaCost} description={description} art={art} baseAttack={baseAttack} baseHp={baseHp} type={type} />
      </div>
      <h3>{typeAndStats}</h3>
      <p>{description}</p>
      <Link href={"/card-editor?card=" + card.id}>Open in Card Editor</Link>
    </>
  );
};

export default function CardTemplate({ data }: { data: { card: CardDef } }) {
  const { card } = data; // data.card holds your post data
  return (
    <PublicSiteLayout title={card?.name ?? "Card"}>
      <PublicPageHeader eyebrow="Card library" title={card?.name ?? "Card"} />
      <ContentPanel variant="document">{card && <CardProperties card={card} />}</ContentPanel>
    </PublicSiteLayout>
  );
}

// once sprites set, need to add a field to query for sprite

/*
export const pageQuery = graphql`
query($path: String!) {
  card(path
:
  {
    eq: $path
  }
)
  {
    name
    baseManaCost
    description
    type
      rarity
    baseAttack
    baseHp
    id
    art
    {
      body
      {
        vertex
        {
          r
          g
          b
          a
        }
      }
      highlight
      {
        r
        g
        b
        a
      }
      primary
      {
        r
        g
        b
        a
      }
      secondary
      {
        r
        g
        b
        a
      }
      shadow
      {
        r
        g
        b
        a
      }
      sprite
      {
        named
      }
    }
  }
  allArt: allFile(filter
:
  {
    extension: {
      eq: "png"
    }
  ,
    relativePath: {
      glob: "**card-images/art/!**"
    }
  }
)
  {
    edges
    {
      node
      {
        name
        childImageSharp
        {
          fluid
          {
            presentationHeight
            presentationWidth
            src
          }
        }
      }
    }
  }
}
`*/
