import jwt, { JwtPayload } from "jsonwebtoken";
import jwksRsa from "jwks-rsa";
import { issuer, keycloakUrl, pgJwtSecret, realm } from "./config";
import { Request, RequestHandler } from "express-serve-static-core";

export type AuthRequest = Request & {
  auth?: JwtPayload;
  admin?: boolean;
};

const client = jwksRsa({
  jwksUri: `${keycloakUrl}/realms/${realm}/protocol/openid-connect/certs`,
  cache: true,
});

const getKey = (header: jwt.JwtHeader, callback: jwt.SigningKeyCallback) =>
  client.getSigningKey(header.kid, (err, key) => {
    if (err || !key) {
      return callback(new Error("Unable to get the key"), undefined);
    }
    callback(null, key.getPublicKey());
  });

export const authenticate: RequestHandler = (req: AuthRequest, res, next) => {
  const token = req.header("Authorization")?.split(" ")[1];

  if (!token) {
    return res.status(401).json({ message: "No token provided" });
  }

  if (token == pgJwtSecret) {
    req.admin = true;
    next();
    return;
  }

  jwt.verify(
    token,
    getKey,
    {
      issuer: issuer,
      algorithms: ["RS256"],
    },
    (err, decoded) => {
      if (err || !decoded) {
        return res.status(401).json({ message: "Invalid token" });
      }

      if (typeof decoded === "string") {
        req.auth = JSON.parse(decoded);
      } else {
        req.auth = decoded;
      }

      next(); // Continue to the next middleware or route handler
    },
  );
};
