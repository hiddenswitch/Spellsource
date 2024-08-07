import NextAuth, { getServerSession, NextAuthOptions } from "next-auth";
import KeycloakProvider from "next-auth/providers/keycloak";
import { JWT } from "next-auth/jwt";
import { clientId, clientSecret, issuer } from "../../../lib/config";
import { NextApiRequest, NextApiResponse } from "next";

const NextOptions: NextAuthOptions = {
  secret: "secret",
  session: {
    strategy: "jwt",
  },
  providers: [
    KeycloakProvider({
      clientId,
      clientSecret,
      issuer,
      checks: ["pkce"],
      authorization: {
        params: { scope: "openid profile email offline_access" },
      },
    }),
  ],
  events: {
    signOut: async ({ token }) => {
      await fetch(`${issuer}/protocol/openid-connect/logout?client_id=${clientId}&id_token_hint=${token.idToken}`);
    },
  },
  callbacks: {
    // include the token as part of the session, for purposes like getting the sub
    session: async ({ session, token, user }) => {
      if (token) {
        session.token = token;
      }
      return session;
    },
    jwt: async ({ token, user, account }) => {
      // initial sign in, add the account properties to the token, as account will be undefined in every subsequent call
      if (account && user) {
        return {
          ...token,
          idToken: account.id_token,
          accessToken: account.access_token,
          refreshToken: account.refresh_token,
          accessTokenExpires: account.expires_at! * 1000,
          refreshTokenExpires: Date.now() + account.refresh_expires_in! * 1000,
          sessionState: account.session_state,
          account,
          user,
        };
      }

      const tokenExpiresIn = token.accessTokenExpires! - Date.now();

      if (tokenExpiresIn <= 1000 * 60) {
        return refreshAccessToken(token);
      } else {
        // console.log(`The token will need refreshing in ${(tokenExpiresIn / 1000).toFixed(0)}s`);
      }

      return token;
    },
  },
};

export default NextAuth(NextOptions);

const emptyResponse = {
  getHeader: () => {},
  setHeader: () => {},
  setCookie: () => {},
} as unknown as NextApiResponse;

export const getSessionDirect = async (req: NextApiRequest) => getServerSession(req, emptyResponse, NextOptions);

// This has to be written manually still as NextAuth doesn't yet have full OIDC support,
// but just the broader OATH2 standard which doesn't standardize token refreshing
const refreshAccessToken = async (token: JWT): Promise<JWT> => {
  try {
    if (Date.now() > token.refreshTokenExpires!) throw Error("Refresh token has expired"); // Refetching is off the table
    const details = {
      client_id: clientId,
      client_secret: clientSecret,
      grant_type: "refresh_token",
      refresh_token: token.refreshToken,
    };
    const body = Object.entries(details)
      .map(([key, value]) => encodeURIComponent(key) + "=" + encodeURIComponent(value!))
      .join("&");
    const url = `${issuer}/protocol/openid-connect/token`;
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      },
      body,
    });
    const refreshedTokens = await response.json();
    if (!response.ok) throw refreshedTokens;
    return {
      ...token,
      accessToken: refreshedTokens.access_token,
      accessTokenExpires: Date.now() + refreshedTokens.expires_in * 1000,
      refreshToken: refreshedTokens.refresh_token ?? token.refreshToken,
      refreshTokenExpires: Date.now() + refreshedTokens.refresh_expires_in * 1000,
      idToken: refreshedTokens.id_token,
    };
  } catch (error) {
    // console.log("errors while refreshing token");
    // console.log(JSON.stringify(error, null, 2));
    return {
      ...token,
      error: "RefreshAccessTokenError",
    };
  }
};
