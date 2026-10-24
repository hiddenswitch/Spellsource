import { useEffect, useRef, useState } from "react";
import CardDisplay, { CardDef } from "./card-display";

import styles from "../creative-layout.module.scss";
import { useRafState } from "react-use";

export const useHoverCardDisplay = (options?: { translateX?: string; translateY?: string }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const [{ docX, docY }, setPosition] = useRafState({ docX: 0, docY: 0 });
  useEffect(() => {
    const move = (event: MouseEvent) => {
      if (ref.current) setPosition({ docX: event.pageX, docY: event.pageY });
    };
    document.addEventListener("mousemove", move);
    return () => document.removeEventListener("mousemove", move);
  }, [setPosition]);
  const [card, setCard] = useState<CardDef | undefined | null>(null);

  const translateX = options?.translateX ?? "0%";
  const translateY = options?.translateY ?? "10%";

  const HoverCardDisplay = () => {
    return card ? (
      <div
        className={styles.hoverCardDisplay}
        style={{
          display: card ? "block" : "none",
          left: `${docX}px`,
          top: `${docY}px`,
          transform: `translateX(${translateX}) translateY(${translateY})`,
        }}
      >
        <CardDisplay {...card} />
      </div>
    ) : (
      <></>
    );
  };

  return { setCard, HoverCardDisplay, ref };
};
