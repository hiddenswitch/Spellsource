import React from "react";
import Header from "./header";
import * as styles from "./creative-layout.module.scss";
import { pages } from "./creative-layout";
import { use100vh } from "react-div-100vh";

export default ({ children }) => {
  const height = use100vh() || 1000;
  return (
    <div className={styles.container} style={{ height }}>
      <Header pages={pages} />
      {children}
    </div>
  );
};
