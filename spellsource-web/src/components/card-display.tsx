import React from "react";
import * as styles from "./card-display.module.scss";

import backgroundLayer from "../../public/static/card-images/layer-1.png";
import baseAttack from "../../public/static/card-images/attack-token.png";
import baseHp from "../../public/static/card-images/health-token.png";
import windowBackground from "../../public/static/card-images/large-card-window-background.png";
import selkieShadow from "../../public/static/card-images/selkie-shadow.png";
import { defaultsDeep } from "lodash";
import { CardDesc } from "../__generated__/spellsource-game";
import { DeepPartial } from "../lib/deep-partial";
import Image from "next/image";

const defaultArt = {
  primary: {
    r: 0.443,
    g: 0.396,
    b: 0.509,
    a: 1.0,
  },
  secondary: {
    r: 0.207,
    g: 0.282,
    b: 0.466,
    a: 1.0,
  },
  shadow: {
    r: 0.107,
    g: 0.182,
    b: 0.366,
    a: 1.0,
  },
  highlight: {
    r: 0.768,
    g: 0.67,
    b: 0.764,
    a: 1.0,
  },
  body: {
    vertex: {
      r: 1.0,
      g: 1.0,
      b: 1.0,
      a: 1.0,
    },
  },
  sprite: {
    named: "Selkie",
    shadow: selkieShadow,
  },
};

export const toRgbaString = (v?: { r: number; g: number; b: number; a: any }) => {
  return v ? `rgba(${v.r * 255}, ${v.g * 255}, ${v.b * 255}, ${v.a})` : undefined;
};

type FixCardDesc<T> = T extends object
  ? {
      [K in keyof T as K extends `${infer name}_` ? name : K]: K extends "unknownFields" ? never : FixCardDesc<T[K]>;
    }
  : T;

export type CardDef = FixCardDesc<CardDesc>;

type DeepReplace<T, V, N> = T extends object
  ? {
      [K in keyof T]: T[K] extends V ? N : DeepReplace<T[K], V, N>;
    }
  : T;

function CardDisplay(props: DeepPartial<CardDef>) {
  let cardArt = defaultsDeep({}, props.art, defaultArt) as CardDef["art"] & typeof defaultArt;
  const art = {
    ...cardArt,
    body: {
      ...cardArt.body,
      vertex: toRgbaString(cardArt.body?.vertex),
    },
    highlight: toRgbaString(cardArt.highlight),
    primary: toRgbaString(cardArt.primary),
    secondary: toRgbaString(cardArt.secondary),
    shadow: toRgbaString(cardArt.shadow),
    sprite: {
      // named: art.sprite.named,
      ...cardArt.sprite,
    },
  };

  const checkTextColor = () => {
    return `-2px 2px ${toRgbaString({
      r: cardArt.body?.vertex?.r / 2,
      g: cardArt.body?.vertex?.g / 2,
      b: cardArt.body?.vertex?.b / 2,
      a: 0.5,
    })}`;
  };

  const checkTokens = () => {
    if (
      (props.baseAttack === null || props.baseAttack === undefined) &&
      (props.baseHp === null || props.baseHp === undefined)
    ) {
      return `none`;
    } else {
      return `initial`;
    }
  };

  return (
    <div className={styles.cardDisplayTemplate}>
      <Image src={backgroundLayer} className={styles.layerOne} priority={true} alt="card" />
      <div className={styles.descriptionBox}>
        <p
          className={styles.description}
          style={{
            color: art.body.vertex,
            textShadow: checkTextColor(),
          }}
        >
          {props.description}
        </p>
        <p className={styles.type}>{props.race !== "NONE" ? props.race : ""}</p>
      </div>
      <p className={styles.baseManaCost}>{props.baseManaCost || 0}</p>
      <p
        className={styles.name}
        style={{
          color: art.body.vertex,
          textShadow: checkTextColor(),
        }}
      >
        {props.name}
      </p>
      <div
        className={styles.primary}
        style={{ background: `linear-gradient(${art.primary}, ${art.primary}) no-repeat` }}
      />
      <div
        className={styles.highlight}
        style={{ background: `linear-gradient(${art.highlight}, ${art.highlight}) no-repeat` }}
      />
      <div
        className={styles.shadow}
        style={{ background: `linear-gradient(${art.shadow}, ${art.shadow}) no-repeat` }}
      />
      <div
        className={styles.secondary}
        style={{ background: `linear-gradient(${art.secondary}, ${art.secondary}) no-repeat` }}
      />
      {props.type === "MINION" && (
        <>
          <div
            className={styles.pedestalPrimary}
            style={{ background: `linear-gradient(${art.primary}, ${art.primary}) no-repeat` }}
          />
          <div
            className={styles.pedestalSecondary}
            style={{ background: `linear-gradient(${art.secondary}, ${art.secondary}) no-repeat` }}
          />
          <div
            className={styles.pedestalShadow}
            style={{ background: `linear-gradient(${art.shadow}, ${art.shadow}) no-repeat` }}
          />
        </>
      )}
      <Image src={windowBackground} className={styles.windowBackground} alt="" />
      <div style={{ display: checkTokens() }}>
        <Image src={baseAttack} className={styles.attackToken} alt="" />
        <p className={styles.baseAttack}>{props.baseAttack || 0}</p>
        <Image src={baseHp} className={styles.hpToken} alt="" />
        <p className={styles.baseHp}>{props.baseHp || 0}</p>
      </div>
      <div className={styles.heroAndShadow}>
        <img src={art.sprite.named["src"] || "/api/art/" + art.sprite.named} className={styles.hero} alt=" " />
        <img src={art.sprite.shadow["src"] || "/api/art/" + art.sprite.shadow} className={styles.heroShadow} alt="" />
      </div>
    </div>
  );
}

export default CardDisplay
