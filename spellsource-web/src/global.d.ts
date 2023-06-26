// Stop imports yelling
declare module "*.png" {
  const value: any
  export = value;
}
declare module "*.gif" {
  const value: any
  export = value;
}

declare global {
  interface Element {
    style: CSSStyleDeclaration;
    innerText: string;
  }
}

import { User } from "next-auth";
import { JWT } from "next-auth/jwt";

declare module "next-auth/jwt" {
  interface JWT {
    idToken?: string;
    accessToken?: string;
    refreshToken?: string;
    accessTokenExpires?: number;
    refreshTokenExpires?: number;
    user?: User;
    error?: any;
    sub?: string;
    name?: string;
    email?: string;
  }
}

declare module "next-auth" {
  interface Session {
    token?: JWT;
    user?: User;
  }

  interface Account {
    refresh_expires_in?: number;
  }
}
