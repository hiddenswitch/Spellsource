import * as styles from "./creative-layout.module.scss";
import {signIn, signOut, useSession} from "next-auth/react";
import React from "react";

export const AuthButton = () => {
  const {status} = useSession();

  return (
    <a className={styles.loginButton} onClick={async () => {
      if (status === "unauthenticated") {
        await signIn("keycloak", {callbackUrl: window.location.href})
      } else if (status === "authenticated") {
        await signOut({callbackUrl: window.location.origin})
      }
    }
    }>
      {status !== "authenticated" ? "Login" : "Logout"}
    </a>
  );
}
