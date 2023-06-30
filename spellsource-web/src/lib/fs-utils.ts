import * as glob from "glob-promise";
import path from "path";
import fs from "fs";
import probe from "probe-image-size";
import { ImageDef } from "../__generated__/client";
import { BlockDef } from "../__generated__/blocks";
import { transformBlock } from "./json-transforms";

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

/*export type ImageDef = {
  src: string,
  width: number,
  height: number,
  name: string,
  id: string
}*/

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

export const saveFile = (contents: string, type: string, name: string) => {
  // Create a Blob from the XML string
  const blob = new Blob([contents], { type });

  // Create an invisible <a> element
  const a = document.createElement("a");
  document.body.appendChild(a);
  a.style.display = "none";

  // Create a URL for the Blob
  const url = window.URL.createObjectURL(blob);
  a.href = url;

  // Set the file name
  a.download = name;

  // Simulate clicking the <a> element
  a.click();

  // Release the created URL
  window.URL.revokeObjectURL(url);

  // Remove the <a> element
  document.body.removeChild(a);
};

export const openFile = (accept: string, onOpen: (result: string) => void) => {
  // Create an invisible file input element
  const input: HTMLInputElement = document.createElement("input");
  input.type = "file";
  input.accept = accept;
  input.style.display = "none";
  input.multiple = true;

  // Append it to the body
  document.body.appendChild(input);

  // This function will be called when the user selects a file
  input.onchange = (event: Event) => {
    for (const file of (event.target as HTMLInputElement).files) {
      const reader: FileReader = new FileReader();

      reader.onload = (e: ProgressEvent<FileReader>) => {
        // The file's text will be printed here
        const result: string = e.target.result as string;

        onOpen(result);
      };

      // Read the file as text
      reader.readAsText(file);
    }
  };

  // Simulate a click on the file input
  input.click();
};

export const getAllBlockJson = async () =>
  (await readAllJson<BlockDef[]>(path.join("src", "blocks", "*.json"))).flat(1).map(transformBlock);

const getAllArt = async () => readAllImages(path.join("card-images", "art", "**", "*.png"));

export const getAllIcons = async () => readAllImages(path.join("assets", "editor", "*.png"));
