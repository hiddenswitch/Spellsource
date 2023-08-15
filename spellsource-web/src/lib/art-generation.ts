import sdxl from "../__generated__/sdxl-ordinary.json";
import { FieldButton } from "../components/blockly/field-button";
import { FieldProgressBar } from "../components/blockly/field-progress-bar";
import { createConfiguration, DefaultApi, PromptNode, server1 } from "../__generated__/comfyclient";
import { newBlock } from "./blockly-utils";
import { BlockSvg, WorkspaceSvg } from "blockly";

export const generateArt = async (p1: any) => {
  const field = p1 as FieldButton;

  const block = field.getSourceBlock();
  if (block.isInFlyout) return;

  const workspace = block.workspace as WorkspaceSvg;

  // const progressBar = field.getSourceBlock().getField("progress") as FieldProgressBar;

  const config = createConfiguration();
  const api = new DefaultApi(config);

  const promptText = JSON.stringify(sdxl)
    .replace("$POSITIVE_TEXT", block.getFieldValue("positive_text"))
    .replace("$NEGATIVE_TEXT", block.getFieldValue("negative_text"));

  const prompt = JSON.parse(promptText) as Record<string, PromptNode>;

  for (let node of Object.values(prompt)) {
    if (node.class_type === "KSampler") {
      node.inputs["seed"] = Math.round(Math.random() * 10000000);
    }
  }

  const digest = await api.apiV1PromptsPost({ requestBody: prompt });

  // const digest = `{"urls": ["/api/v1/images/320557c86397912cfe54d5b03ffe0efffd9a3a97f4a9fcb4844c92a7853a5326", "http://0.0.0.0:8188/view?filename=ComfyUI_temp_zphhv_00001_.png&type=output"]}`;

  console.log(digest);

  if (!digest) return;

  const [relativeUrl] = JSON.parse(digest)["urls"];
  const artOutput = newBlock(workspace, "Art_Output");
  const artGenerated = newBlock(workspace, "Art_Generated");
  (artGenerated as BlockSvg).initSvg();
  (artOutput as BlockSvg).initSvg();
  artGenerated.setFieldValue(config.baseServer["url"] + relativeUrl, "src");
  artOutput.getInput("art").connection.connect(artGenerated.outputConnection);
  const targetBlock = block.nextConnection.targetBlock();
  if (targetBlock) {
    targetBlock.previousConnection.connect(artOutput.nextConnection);
  }
  block.nextConnection.connect(artOutput.previousConnection);

  workspace.render();
};
