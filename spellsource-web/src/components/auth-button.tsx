import * as styles from "./creative-layout.module.scss";
import { signIn, signOut, useSession } from "next-auth/react";
import React from "react";

export const AuthButton = () => {
  const { status } = useSession();
  const label = status !== "authenticated" ? "Login" : "Logout";
  return (
    <a
      aria-label={label}
      className={styles.loginButton}
      onClick={async () => {
        if (status === "authenticated") {
          await signOut({ callbackUrl: window.location.origin });
        } else {
          await signIn("keycloak", {
            callbackUrl: window.location.pathname !== "/" ? window.location.href : window.location.href + "/home",
          });
        }
      }}
    >
      {label}
    </a>
  );
};
