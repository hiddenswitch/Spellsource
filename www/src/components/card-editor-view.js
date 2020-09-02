import {Form} from "react-bootstrap";
import Blockly from "blockly";
import AceEditor from "react-ace";
import React, {useState} from "react";
import CardEditorWorkspace from "./card-editor-workspace";

const CardEditorView = () => {
  const [code, setCode] = useState(``)
  const [query, setQuery] = useState(``)
  const [showCatalogueBlocks, setShowCatalogueBlocks] = useState(false)
  const [showBlockComments, setShowBlockComments] = useState(true)
  const blockCommentsTooltip = 'Toggles the helpful/informational comments that display on certain blocks in the toolbox'
  const catalogueBlocksTooltip = 'Toggles whether the blocks for real cards from the catalogue show up in search'

  const search = evt => {
    const query = evt.target.value
    setQuery(query)
  }

  const toggleCatalogueBlocks = evt => {
    setShowCatalogueBlocks(!showCatalogueBlocks)
    Blockly.getMainWorkspace().getToolbox().clearSelection()
    if (query.length > 0) {
      search({target: {value: query}})
    }
  }

  const toggleBlockTooltips = evt => {
    setShowBlockComments(!showBlockComments)
    Blockly.getMainWorkspace().getToolbox().clearSelection()
    Blockly.getMainWorkspace().hideSpellsourceComments = showBlockComments
  }


  return (<span>
      <Form.Control type="text"
                    placeholder={'Search blocks'}
                    value={query}
                    onChange={e => search(e)}
                    style={{width: '40%'}}
      />
      <Form.Check style={{display: 'inline'}}>
        <Form.Check.Input defaultChcked={showCatalogueBlocks}
                          onChange={e => toggleCatalogueBlocks(e)}
                          value={showCatalogueBlocks}
                          title={catalogueBlocksTooltip}
                          style={
                            {
                              height: '15px',
                              width: '15px',
                              webkitAppearance: 'checkbox'
                            }
                          }
        />
        <Form.Check.Label title={catalogueBlocksTooltip}> Search Card Catalogue   </Form.Check.Label>
      </Form.Check>
      <Form.Check style={{display: 'inline'}}>
        <Form.Check.Input defaultChecked={showBlockComments}
                          onChange={e => toggleBlockTooltips(e)}
                          value={showBlockComments}
                          title={blockCommentsTooltip}
                          style={
                            {
                              height: '15px',
                              width: '15px',
                              webkitAppearance: 'checkbox'
                            }
                          }
        />
        <Form.Check.Label title={blockCommentsTooltip}> Show Toolbox Comments</Form.Check.Label>
      </Form.Check>
    <CardEditorWorkspace setCode={setCode}
                         showCatalogueBlocks={showCatalogueBlocks}
                         query={query}
    />
    <AceEditor
      width={'100%'}
      mode="json"
      theme="github"
      setOptions={{
        'wrap': true
      }}
      readOnly={true}
      value={code}
      editorProps={{$blockScrolling: true}}
    />
  </span>)
}

export default CardEditorView