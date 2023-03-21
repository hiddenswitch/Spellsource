import * as glob from "glob-promise";
import path from "path";
import fs from "fs";
import probe from "probe-image-size";

/**
 * Parses the JSON for all matching files
 * @param globPath glob pattern for files, relative to the root spellsource-web folder
 * @param transform
 */
export const readAllJson = async <T = any>(globPath: string, transform?: (json: T, file: string) => void) => {
  const files = await glob.promise(path.join(process.cwd(), globPath));
  return await Promise.all(files.map(async file => {
    const fileText = await fs.promises.readFile(file, {encoding: "utf8"});
    const json = JSON.parse(fileText) as T;
    transform?.(json, file)
    return json;
  }));
}

export type ImageDef = {
  src: string,
  width: number,
  height: number,
  name: string,
  id: string
}

/**
 * Gets the src and width/height of all matching files
 * @param globPath glob pattern for files, relative to the static folder
 */
export const readAllImages = async (globPath: string) => {
  const publicPath = path.join(process.cwd(), "public");
  const artFiles = await glob.promise(path.join(publicPath, "static", globPath));

  return await Promise.all(artFiles.map(async (artPath) => {
    const {width, height} = await probe(fs.createReadStream(artPath));
    return {
      src: path.relative(publicPath, artPath).replaceAll(path.sep, "/"),
      width,
      height,
      name: path.basename(artPath, ".png"),
      id: path.basename(artPath, ".png"),
    } as ImageDef
  }))
}
