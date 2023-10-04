import sdxl from "../__generated__/sdxl-ordinary.json";
import { FieldButton } from "../components/blockly/field-button";
import { createConfiguration } from "../__generated__/comfyclient/configuration";
import { argsList, newBlock } from "./blockly-utils";
import Blockly, { BlockSvg, WorkspaceSvg } from "blockly";
import { ContextType } from "react";
import { BlocklyDataContext } from "../pages/card-editor";
import { PromptNode } from "../__generated__/comfyclient/models/PromptNode";
import { ObjectDefaultApi as DefaultApi } from "../__generated__/comfyclient/types/ObjectParamAPI";
import { FetchResult } from "@apollo/client";
import { GenerateArtMutation, MutationGenerateArtArgs } from "../__generated__/client";
import { comfyUrl } from "./config";

type BlockWithPrivate = Blockly.Block & {
  _interval?: NodeJS.Timer | number;
  _hash?: string;
};
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

  const config = createConfiguration();
  const api = new DefaultApi(config);

  const promptText = JSON.stringify(sdxl)
    .replace("$POSITIVE_TEXT", block.getFieldValue("positive_text").trim())
    .replace("$NEGATIVE_TEXT", block.getFieldValue("negative_text").trim());

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
  }, 1000) as NodeJS.Timer | number;
  block["_hash"] = await generateHash(prompt);

  const workspace = block.workspace as WorkspaceSvg & {
    _data: ContextType<typeof BlocklyDataContext>;
  };
  const { generateArt } = workspace["_data"];

  generateArt!({
    variables: {
      positiveText: block.getFieldValue("positive_text"),
      negativeText: block.getFieldValue("negative_text"),
      seed: parseInt(block.getFieldValue("seed")),
    },
  })
    .then(onGenerateArt(block))
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

export const getPrompt = (args: MutationGenerateArtArgs) => {
  const { positiveText, negativeText, seed } = args;

  const promptText = JSON.stringify(sdxl)
    .replace("$POSITIVE_TEXT", positiveText.trim())
    .replace("$NEGATIVE_TEXT", negativeText.trim());

  const prompt = JSON.parse(promptText) as Record<string, PromptNode>;

  for (let node of Object.values(prompt)) {
    if (node.class_type === "KSampler") {
      node.inputs["seed"] = seed!;
    }
  }

  return prompt;
};

const onGenerateArt =
  (block: BlockWithPrivate) => async (response: FetchResult<GenerateArtMutation> | undefined | null) => {
    const hash = block["_hash"];
    const result = response?.data?.generateArt;
    if (!result || !hash) {
      return;
    }
    const urls = result["urls"] as string[];
    const [relativeUrl] = urls;

    if (block.isInFlyout || block.isDisposed() || !relativeUrl) return;

    if (!relativeUrl.includes(hash)) {
      console.log("not generating for canceled request");
      return;
    }

    const workspace = block.workspace as WorkspaceSvg & {
      _data: ContextType<typeof BlocklyDataContext>;
    };
    const artGenerated = newBlock(workspace, "Art_Generated");
    (artGenerated as BlockSvg).initSvg();
    artGenerated.setFieldValue(comfyUrl + relativeUrl, "src");
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
    await saveGeneratedArt!({ variables: { hash, urls: ["/api/art/generated/" + hash, comfyUrl + relativeUrl] } });

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
