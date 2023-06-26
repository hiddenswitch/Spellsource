import fs from "fs";
import path from "path";

module.exports = {
  walk: async function* walk(dir) {
    for await (const d of await fs.promises.opendir(dir)) {
      const entry = path.join(dir, d.name);
      if (d.isDirectory()) {
        yield* walk(entry);
      } else if (d.isFile()) {
        yield entry;
      }
    }
  },
  walkSync: function* walkSync(dir) {
    const openedDir = fs.opendirSync(dir);
    try {
      let d = openedDir.readSync();
      while (d != null) {
        const entry = path.join(dir, d.name);
        if (d.isDirectory()) {
          yield* walkSync(entry);
        } else if (d.isFile()) {
          yield entry;
        }
        d = openedDir.readSync();
      }
    } finally {
      openedDir.closeSync();
    }
  },
};