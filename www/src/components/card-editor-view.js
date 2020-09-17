import {Form} from "react-bootstrap";
import styles from './card-editor-view.module.css'
import Blockly from "blockly";
import AceEditor from "react-ace";
import React, {useRef, useState} from "react";
import CardEditorWorkspace from "./card-editor-workspace";
import useComponentWillMount from "../hooks/use-component-will-mount";

const CardEditorView = (props) => {

  const [code, setCode] = useState(``)
  const [query, setQuery] = useState(``)
  const [showCatalogueBlocks, setShowCatalogueBlocks] = useState(false)
  const [showBlockComments, setShowBlockComments] = useState(true)
  const [compactBlocks, setCompactBlocks] = useState(true)
  const [showJSON, setShowJSON] = useState(true)
  const blockCommentsTooltip = 'Toggles the helpful/informational comments that display on certain blocks in the toolbox'
  const catalogueBlocksTooltip = 'Toggles whether the blocks for real cards from the catalogue show up in search'
  const compactBlocksTooltip = 'Render the blocks compactly rather than as always full rectangles'
  const showJSONTooltip = 'Show the JSON representation of the workspace below'

  const catalogueBlocksCheck = useRef(null)
  const catalogueBlocksLabel = useRef(null)
  const blockCommentsCheck = useRef(null)
  const blockCommentsLabel = useRef(null)
  const compactBlocksCheck = useRef(null)
  const compactBlocksLabel = useRef(null)
  const showJSONCheck = useRef(null)
  const showJSONLabel = useRef(null)

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

  const addTooltip = (ref, tooltip) => {
    if (!!ref.current) {
      ref.current.tooltip = tooltip
      Blockly.Tooltip.bindMouseEvents(ref.current)
    } else {
      setTimeout(() => addTooltip(ref, tooltip), 100)
    }
  }

  useComponentWillMount(() => {
    addTooltip(catalogueBlocksCheck, catalogueBlocksTooltip)
    addTooltip(catalogueBlocksLabel, catalogueBlocksTooltip)
    addTooltip(blockCommentsCheck, blockCommentsTooltip)
    addTooltip(blockCommentsLabel, blockCommentsTooltip)
    addTooltip(compactBlocksCheck, compactBlocksTooltip)
    addTooltip(compactBlocksLabel, compactBlocksTooltip)
    addTooltip(showJSONCheck, showJSONTooltip)
    addTooltip(showJSONLabel, showJSONTooltip)
  })

  return (<span>
      <Form.Control type="text"
                    placeholder={'Search blocks'}
                    value={query}
                    onChange={e => search(e)}
                    className={styles.editorSearch}
      />
      <Form.Check className={styles.editorOption}>
        <Form.Check.Input defaultChcked={showCatalogueBlocks}
                          onChange={e => toggleCatalogueBlocks(e)}
                          value={showCatalogueBlocks}
                          className={styles.editorCheck}
                          ref={catalogueBlocksCheck}
                          bsPrefix={styles.unselectable}
        />
        <Form.Check.Label bsPrefix={styles.unselectable}
                          ref={catalogueBlocksLabel}> Search Card Catalogue</Form.Check.Label>
      </Form.Check>
    <CardEditorWorkspace setCode={setCode}
                         showCatalogueBlocks={showCatalogueBlocks}
                         query={query}
                         defaultCard={props.defaultCard}
                         renderer={compactBlocks ? 'spellsource' : 'geras'}
    />
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={showBlockComments}
                        onChange={e => toggleBlockTooltips(e)}
                        value={showBlockComments}
                        className={styles.editorCheck}
                        ref={blockCommentsCheck}
                        bsPrefix={styles.unselectable}
      />
      <Form.Check.Label ref={blockCommentsLabel}
                        bsPrefix={styles.unselectable}> Show Toolbox Comments</Form.Check.Label>
    </Form.Check>
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={compactBlocks}
                        onChange={e => setCompactBlocks(!compactBlocks)}
                        value={compactBlocks}
                        className={styles.editorCheck}
                        ref={compactBlocksCheck}
                        bsPrefix={styles.unselectable}
      />
      <Form.Check.Label ref={compactBlocksLabel}
                        bsPrefix={styles.unselectable}> Compact Blocks</Form.Check.Label>
    </Form.Check>
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={showJSON}
                        onChange={e => setShowJSON(!showJSON)}
                        value={showJSON}
                        className={styles.editorCheck}
                        ref={showJSONCheck}
                        bsPrefix={styles.unselectable}
      />
      <Form.Check.Label ref={showJSONLabel}
                        bsPrefix={styles.unselectable}> Show JSON</Form.Check.Label>
    </Form.Check>
    {
      showJSON ? <AceEditor
        width={'100%'}
        mode="json"
        theme="github"
        setOptions={{
          'wrap': true
        }}
        readOnly={true}
        value={code}
        editorProps={{$blockScrolling: true}}
      /> : <div/>
    }

  </span>)
}

export default CardEditorView