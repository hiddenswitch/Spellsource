import * as glob from "glob-promise";
import path from "path";
import fs from "fs";
import probe from "probe-image-size";
import { transformBlock } from "./json-transforms";
import { BlockDef } from "./blockly-types";
import { ImageDef } from "./art-generation";

/**
 * Parses the JSON for all matching files
 * @param globPath glob pattern for files, relative to the root spellsource-web folder
 * @param transform
 */
export const readAllJson = async <T = any>(globPath: string, transform?: (json: T, file: string) => void) => {
  const files = await glob.promise(path.join(process.cwd(), globPath));
  return await Promise.all(
    files.map(async (file) => {
      const fileText = await fs.promises.readFile(file, { encoding: "utf8" });
      try {
        JSON.parse(fileText);
      } catch (e) {
        console.error(`failed to parse json ${file}`);
      }
      const json = JSON.parse(fileText) as T;
      transform?.(json, file);
      return json;
    })
  );
};

export const readAllJsonSync = <T = any>(globPath: string, transform?: (json: T, file: string) => void) => {
  const files = glob.sync(path.join(process.cwd(), globPath));
  return files.map((file: string) => {
    const fileText = fs.readFileSync(file, { encoding: "utf8" });
    try {
      JSON.parse(fileText);
    } catch (e) {
      console.error(`failed to parse json ${file}`);
    }
    const json = JSON.parse(fileText) as T;
    transform?.(json, file);
    return json;
  });
};

/**
 * Gets the src and width/height of all matching files
 * @param globPath glob pattern for files, relative to the static folder
 */
export const readAllImages = async (globPath: string) => {
  const publicPath = path.join(process.cwd(), "public");
  const artFiles = await glob.promise(path.join(publicPath, "static", globPath));

  return await Promise.all(
    artFiles.map(async (artPath) => {
      const { width, height } = await probe(fs.createReadStream(artPath));
      return {
        src: path.relative(publicPath, artPath).replaceAll(path.sep, "/"),
        width,
        height,
        name: path.basename(artPath, ".png"),
        id: path.basename(artPath, ".png"),
      } as ImageDef;
    })
  );
};

export const getAllBlockJson = async () => (await readAllJson<BlockDef[]>(path.join("src", "blocks", "*.json"))).flat(1).map(transformBlock);

export const getAllArt = async () => readAllImages(path.join("card-images", "art", "**", "*.png"));

export const getAllIcons = async () => readAllImages(path.join("assets", "editor", "*.png"));
