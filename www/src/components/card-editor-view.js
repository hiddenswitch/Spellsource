import {Button, Form} from "react-bootstrap";
import styles from './card-editor-view.module.css'
import Blockly from "blockly";
import AceEditor from "react-ace";
import React, {useRef, useState} from "react";
import CardEditorWorkspace from "./card-editor-workspace";
import useComponentWillMount from "../hooks/use-component-will-mount";
import CardTesterWorkspace from "./card-tester-workspace";

const CardEditorView = (props) => {

  const [code, setCode] = useState(``)
  const [query, setQuery] = useState(``)
  const [showCatalogueBlocks, setShowCatalogueBlocks] = useState(false)
  const [compactBlocks, setCompactBlocks] = useState(true)
  const [showJSON, setShowJSON] = useState(false)
  const [showJS, setShowJS] = useState(false)
  const catalogueBlocksTooltip = 'Toggles whether the blocks for real cards from the catalogue show up in search'
  const compactBlocksTooltip = 'Render the blocks compactly rather than as always full rectangles'
  const showJSONTooltip = 'Show the JSON representation of the blocks in the workspace'
  const showJSTooltip = 'Show the JS representation of the code in the workspace'

  const catalogueBlocksCheck = useRef(null)
  const catalogueBlocksLabel = useRef(null)
  const compactBlocksCheck = useRef(null)
  const compactBlocksLabel = useRef(null)
  const showJSONCheck = useRef(null)
  const showJSONLabel = useRef(null)
  const showJSCheck = useRef(null)
  const showJSLabel = useRef(null)

  const blocklyEdior = useRef(null)

  const [realCode, setRealCode] = useState(``)

  const workspace = () => {
    return blocklyEdior.current.workspace
  }

  const search = evt => {
    const query = evt.target.value
    setQuery(query)
  }

  const toggleCatalogueBlocks = evt => {
    setShowCatalogueBlocks(!showCatalogueBlocks)
    workspace().getToolbox().clearSelection()
    if (query.length > 0) {
      search({target: {value: query}})
    }
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
    addTooltip(compactBlocksCheck, compactBlocksTooltip)
    addTooltip(compactBlocksLabel, compactBlocksTooltip)
    addTooltip(showJSONCheck, showJSONTooltip)
    addTooltip(showJSONLabel, showJSONTooltip)
    addTooltip(showJSCheck, showJSTooltip)
    addTooltip(showJSLabel, showJSTooltip)
  })

  return (<span>
    <Form.Control type="text"
                  placeholder={'Search blocks'}
                  value={query}
                  onChange={e => search(e)}
                  className={styles.editorSearch}
    />
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={showCatalogueBlocks}
                        onChange={e => toggleCatalogueBlocks(e)}
                        value={showCatalogueBlocks}
                        className={styles.editorCheck}
                        ref={catalogueBlocksCheck}
      />
      <Form.Check.Label ref={showJSONLabel}> Search Card Catalogue</Form.Check.Label>
    </Form.Check>
    <CardEditorWorkspace setJSON={setCode}
                         setJS={setRealCode}
                         showCatalogueBlocks={showCatalogueBlocks}
                         query={query}
                         defaultCard={props.defaultCard}
                         renderer={compactBlocks ? 'spellsource' : 'geras'}
                         ref={blocklyEdior}
    />
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={compactBlocks}
                        onChange={e => setCompactBlocks(!compactBlocks)}
                        value={compactBlocks}
                        className={styles.editorCheck}
                        ref={compactBlocksCheck}
      />
      <Form.Check.Label ref={compactBlocksLabel}> Compact Blocks</Form.Check.Label>
    </Form.Check>
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={showJSON}
                        onChange={e => setShowJSON(!showJSON)}
                        value={showJSON}
                        className={styles.editorCheck}
                        ref={showJSONCheck}
      />
      <Form.Check.Label ref={showJSONLabel}> Show JSON</Form.Check.Label>
    </Form.Check>
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input defaultChecked={showJS}
                        onChange={e => setShowJS(!showJS)}
                        value={showJS}
                        className={styles.editorCheck}
                        ref={showJSCheck}
      />
      <Form.Check.Label ref={showJSLabel}> Show JS</Form.Check.Label>
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
    {
      showJS ? <AceEditor
        width={'100%'}
        height={'200px'}
        mode="javascript"
        theme="github"
        setOptions={{
          'wrap': true
        }}
        readOnly={true}
        value={realCode}
        editorProps={{$blockScrolling: true}}
      /> : <div/>
    }
  </span>)
}

export default CardEditorView