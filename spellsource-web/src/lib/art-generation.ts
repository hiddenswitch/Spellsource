import sdxl from "../__generated__/sdxl-ordinary.json";
import { FieldButton } from "../components/blockly/field-button";
import { argsList, newBlock } from "./blockly-utils";
import Blockly, { BlockSvg, WorkspaceSvg } from "blockly";
import { ContextType } from "react";
import { BlocklyDataContext } from "../pages/card-editor";
import { client as api, PromptNode } from "../__generated__/comfyclient";
import { comfyUrl } from "./config";
import { isArray } from "lodash";

type BlockWithPrivate = Blockly.Block & {
  _interval?: number | ReturnType<typeof setInterval>;
  _hash?: string;
};

export type ImageDef = {
  id: string;
  name: string;
  src: string;
  height: number;
  width: number;
};

export interface MutationGenerateArtArgs {
  positiveText: string;
  negativeText: string;
  seed: string;
}

export const randomizeSeed = (p1: any) => {
  const field = p1 as FieldButton;
  const block = field.getSourceBlock()!;
  if (block.isInFlyout) return;

  block.setFieldValue(Math.round(Math.random() * 10000000), "seed");
};

export const generateArt = async (p1: any) => {
  const field = p1 as FieldButton;

  const block = field.getSourceBlock()! as BlockWithPrivate;
  if (block.isInFlyout) return;

  if (block.getFieldValue("button") === "Stop" || block["_interval"] || block["_hash"]) {
    onRequestStop(block);
    return;
  }

  api.setConfig({
    baseUrl: comfyUrl,
  });

  const promptText = JSON.stringify(sdxl).replace("$POSITIVE_TEXT", block.getFieldValue("positive_text").trim()).replace("$NEGATIVE_TEXT", block.getFieldValue("negative_text").trim());

  const prompt = JSON.parse(promptText) as Record<string, PromptNode>;

  for (let node of Object.values(prompt)) {
    if (node.class_type === "KSampler") {
      node.inputs["seed"] = parseInt(block.getFieldValue("seed"));
    }
  }

  block.setFieldValue("Stop", "button");
  block.setFieldValue(`Processing...`, "counter");

  let elapsed = 0;

  block["_interval"] = setInterval(() => {
    elapsed++;
    block.setFieldValue(`Processing... ${elapsed}s`, "counter");
  }, 1000);
  block["_hash"] = await generateHash(prompt);

  fetch("/api/art/generate", {
    method: "POST",
    body: JSON.stringify({
      positiveText: block.getFieldValue("positive_text"),
      negativeText: block.getFieldValue("negative_text"),
      seed: parseInt(block.getFieldValue("seed")),
    }),
  })
    .then(async (res) => {
      if (isArray(res.body)) {
        await onGenerateArt(block, res.body);
      }
    })
    .finally(() => onRequestStop(block));
};

const onRequestStop = (block: BlockWithPrivate) => {
  delete block["_hash"];
  if (block["_interval"]) {
    clearInterval(block["_interval"]);
    delete block["_interval"];
  }

  if (!block.isDisposed()) {
    const baseArgs = Object.fromEntries(argsList(Blockly.Blocks[block.type].json).map((arg) => [arg.name, arg]));
    block.setFieldValue(baseArgs["counter"].text, "counter");
    block.setFieldValue(baseArgs["button"].text, "button");
  }
};

const onGenerateArt = async (block: BlockWithPrivate, response: string[] | undefined | null) => {
  const hash = block["_hash"];
  if (!response || !hash) {
    console.log("canceled request");
    return;
  }
  const url = response.at(-1);

  if (!url || block.isInFlyout || block.isDisposed()) return;

  const workspace = block.workspace as WorkspaceSvg & {
    _data: ContextType<typeof BlocklyDataContext>;
  };
  const artGenerated = newBlock(workspace, "Art_Generated");
  (artGenerated as BlockSvg).initSvg();
  artGenerated.setFieldValue(url, "src");
  artGenerated.setFieldValue(hash, "hash");
  const artOutput = newBlock(workspace, "Art_Output");
  (artOutput as BlockSvg).initSvg();
  artOutput.getInput("art")!.connection!.connect(artGenerated.outputConnection!);
  const targetBlock = block.nextConnection!.targetBlock();
  if (targetBlock) {
    targetBlock.previousConnection!.connect(artOutput.nextConnection!);
  }
  block.nextConnection!.connect(artOutput.previousConnection!);

  workspace.render();

  const { saveGeneratedArt, refreshGeneratedArt } = workspace["_data"];

  // TODO make this happen serverside
  await saveGeneratedArt!({ variables: { hash, urls: ["/api/art/generated/" + hash, url] } });

  await refreshGeneratedArt!();
};

async function generateHash(body: object) {
  // Stringify and sort keys in the JSON object
  let str = JSON.stringify(body);

  // Encode the string as a Uint8Array
  let encoder = new TextEncoder();
  let data = encoder.encode(str);

  // Create a SHA-256 hash of the data
  let hash = await window.crypto.subtle.digest("SHA-256", data);

  // Convert the hash (which is an ArrayBuffer) to a hex string
  let hashArray = Array.from(new Uint8Array(hash));
  let hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");

  return hashHex;
}
