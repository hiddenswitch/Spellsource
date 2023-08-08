import { useRef, useState } from "react";
import CardDisplay, { CardDef } from "./card-display";

import styles from "../creative-layout.module.scss";
import { useMouse } from "react-use";

export const useHoverCardDisplay = (options?: { translateX?: string; translateY?: string }) => {
  const ref = useRef(null);
  const { docX, docY } = useMouse(ref);
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
