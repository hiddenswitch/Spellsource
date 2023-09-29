import { Client } from "pg";
import { pgDatabase, pgHost, pgPassword, pgPort, pgUser } from "../../lib/config";
import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  // Create a new client instance with a connection timeout of 1000 milliseconds
  const client = new Client({
    user: pgUser,
    password: pgPassword,
    host: pgHost,
    database: pgDatabase,
    port: pgPort,
    connectionTimeoutMillis: 1000, // Set connection timeout to 1 second
  });

  try {
    // Connect to the PostgreSQL database
    await client.connect();

    const result = await client.query(`
            SELECT success
            FROM hiddenswitch.flyway_schema_history
            ORDER BY installed_rank DESC
            LIMIT 1
        `);

    if (result.rows[0]?.success) {
      res.status(200).send("OK");
    } else {
      res.status(500).send("Flyway Migration Unsuccessful");
    }
  } catch (err) {
    res.status(500).send("Database Unreachable or Migration Unsuccessful");
  } finally {
    await client.end();
  }
}
