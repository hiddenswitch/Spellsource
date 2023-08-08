import React from "react";
import * as styles from "../creative-layout.module.scss";
import spellsourceLogo from "../../../public/static/assets/spellsource.png";
import cloud from "../../../public/static/assets/transparent-cloud.png";
import Image from "next/image";

function HeroLogoSection() {
  return (
    <div className={`${styles.heroSpellsourceLogo}`}>
      <Image alt={"Logo"} src={spellsourceLogo} className={styles.spellsourceLogo} />
      <Image
        alt={"Cloud"}
        src={cloud}
        style={{ top: "10%", left: "5%" }}
        className={`${styles.cloud} ${styles.mcloud}`}
      />
      <Image
        alt={"Cloud"}
        src={cloud}
        style={{ bottom: "10%", left: "10%" }}
        className={`${styles.cloud} ${styles.lcloud}`}
      />
      <Image
        alt={"Cloud"}
        src={cloud}
        style={{ top: "15%", right: "3%" }}
        className={`${styles.cloud} ${styles.mcloud}`}
      />
      <Image
        alt={"Cloud"}
        src={cloud}
        style={{ bottom: "-5%", right: "8%" }}
        className={`${styles.cloud} ${styles.scloud}`}
      />
    </div>
  );
}

export default HeroLogoSection;
