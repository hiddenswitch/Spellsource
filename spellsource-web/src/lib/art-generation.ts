import sdxl from "../__generated__/sdxl-ordinary.json";
import { FieldButton } from "../components/blockly/field-button";
import { createConfiguration, DefaultApi, PromptNode } from "../__generated__/comfyclient";
import { argsList, newBlock } from "./blockly-utils";
import Blockly, { Block, BlockSvg, WorkspaceSvg } from "blockly";
import { ContextType } from "react";
import { BlocklyDataContext } from "../pages/card-editor";

export const randomizeSeed = (p1: any) => {
  const field = p1 as FieldButton;
  const block = field.getSourceBlock();
  if (block.isInFlyout) return;

  block.setFieldValue(Math.round(Math.random() * 10000000), "seed");
};

export const generateArt = async (p1: any) => {
  const field = p1 as FieldButton;

  const block = field.getSourceBlock();
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
  }, 1000);
  block["_hash"] = await generateHash(prompt);

  fetch("http://localhost:8188/api/v1/prompts", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(prompt),
  })
    .then(onGenerateArt(block))
    .finally(() => onRequestStop(block));
};

const onRequestStop = (block: Block) => {
  delete block["_hash"];
  if (block["_interval"]) {
    clearInterval(block["_interval"]);
    delete block["_interval"];
  }

  if (!block.isDisposed()) {
    const baseArgs = Object.fromEntries(argsList(Blockly.Blocks["ArtGen_Factory"].json).map((arg) => [arg.name, arg]));
    block.setFieldValue(baseArgs["counter"].text, "counter");
    block.setFieldValue(baseArgs["button"].text, "button");
  }
};

const onGenerateArt = (block: Block) => async (response: Response | undefined | null) => {
  const hash = block["_hash"];
  const result = await response?.json();
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

  const workspace = block.workspace as WorkspaceSvg;
  const artOutput = newBlock(workspace, "Art_Output");
  const artGenerated = newBlock(workspace, "Art_Generated");
  (artGenerated as BlockSvg).initSvg();
  (artOutput as BlockSvg).initSvg();
  artGenerated.setFieldValue(createConfiguration().baseServer["url"] + relativeUrl, "src");
  artGenerated.setFieldValue(hash, "hash");
  artOutput.getInput("art").connection.connect(artGenerated.outputConnection);
  const targetBlock = block.nextConnection.targetBlock();
  if (targetBlock) {
    targetBlock.previousConnection.connect(artOutput.nextConnection);
  }
  block.nextConnection.connect(artOutput.previousConnection);

  workspace.render();

  const { saveGeneratedArt, refreshGeneratedArt } = workspace["_data"] as ContextType<typeof BlocklyDataContext>;

  await saveGeneratedArt({ variables: { hash, urls } });
  await refreshGeneratedArt();
};

async function generateHash(body) {
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
