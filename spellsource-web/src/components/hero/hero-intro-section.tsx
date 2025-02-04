import React, { CSSProperties, FunctionComponent, ReactNode } from "react";
import * as styles from "../creative-layout.module.scss";
import harvard from "../../../public/static/assets/Harvard.png";
import mit from "../../../public/static/assets/MIT.png";
import mozilla from "../../../public/static/assets/Mozilla.png";
import un from "../../../public/static/assets/UnitedNations.png";
import unity from "../../../public/static/assets/Unity.png";
import Link from "next/link";
import Image from "next/image";
import { ReactNodeLike } from "prop-types";

function HeroIntroSection() {
  const title = "Let’s rethink game development";
  const body = "Spellsource is a community-driven, open-source digital card game where every card is free. The Spellsource community has authored thousands of cards and continues to grow. Learn more about the game here.";

  const logos = [mozilla, mit, harvard, un, unity];
  const images = logos.map((image, i) => <Image alt={""} key={i} src={image} className={styles.logos} />);

  return (
    <div className={`${styles.heroContainer} ${styles.backgroundGradient1}`}>
      <div className={styles.heroTextContainer}>
        <div className={styles.heroTitle}>
          <h1>{title}</h1>
        </div>
        <div className={styles.heroBody}>
          <h2>{body}</h2>
        </div>
      </div>
      <div className={styles.heroButtonsContainer}>
        <Button title="Play Now" buttonStyle="dark" route={"/download"} />
        <Button title="Join Discord" buttonStyle="light" route={"https://discord.gg/HmbESh2"} />
      </div>
      <div className={styles.heroIntroSubText}>Proudly supported by</div>
      <div className={styles.heroIntroLogos}>{images}</div>
    </div>
  );
}

const Button: FunctionComponent<{
  title: string | ReactNode;
  route: string;
  buttonStyle: "light" | "dark";
}> = ({ title, route, buttonStyle }) => {
  const light: CSSProperties = {
    color: "#1B1B1B",
    border: "2px solid #1B1B1B",
    borderRadius: "12px",
    fontSize: "32px",
    fontWeight: "bold",
    padding: "10px 22px",
    textAlign: "center",
    textDecoration: "none",
    width: "250px",
  };
  const dark: CSSProperties = {
    color: "#fff",
    backgroundColor: "#2837FF",
    border: "2px solid #2837FF",
    borderRadius: "12px",
    fontSize: "32px",
    fontWeight: "bold",
    padding: "10px 22px",
    textAlign: "center",
    textDecoration: "none",
    marginRight: "30px",
    width: "250px",
  };

  return (
    <Link href={route} style={buttonStyle == "light" ? light : dark}>
      {title}
    </Link>
  );
};

export default HeroIntroSection;
