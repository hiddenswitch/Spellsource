import dynamic from "next/dynamic";
import Layout from "../components/card-editor-layout";
import * as BlocklyMiscUtils from "../lib/blockly-spellsource-utils";

const CardEditorWorkspace = dynamic(
  async () => {
    const [Blockly, workspace] = await Promise.all([
      import("blockly"),
      import("../components/card-editor-workspace"),
    ]);
    BlocklyMiscUtils.loadableInit(Blockly);
    return workspace.default;
  },
  { ssr: false, loading: () => <span>Loading</span> },
);

export default function CardEditorExample() {
  return (
    <Layout>
      <CardEditorWorkspace defaultCard={true} />
    </Layout>
  );
}
