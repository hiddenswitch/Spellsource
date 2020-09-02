import React from "react";
import CardEditorWorkspace from "./card-editor-workspace";

const CardEditorView2 = () => {

  return (<span>
    <CardEditorWorkspace setCode={()=>{}}
                         showCatalogueBlocks={false}
                         query={``}
    />
  </span>)
}

export default CardEditorView2