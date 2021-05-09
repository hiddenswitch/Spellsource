import {Button, Form} from "react-bootstrap";
import * as styles from './card-editor-view.module.css'
import Blockly from "blockly";
import AceEditor from "react-ace";
import React, {useRef, useState} from "react";
import CardEditorWorkspace from "./card-editor-workspace";
import useComponentWillMount from "../hooks/use-component-will-mount";

const CardEditorView = (props) => {

  const [code, setCode] = useState(``)
  const [query, setQuery] = useState(``)
  const [searchCatalogueBlocks, setSearchCatalogueBlocks] = useState(false)
  const [searchArtBlocks, setSearchArtBlocks] = useState(false)
  const [compactBlocks, setCompactBlocks] = useState(true)
  const [showJSON, setShowJSON] = useState(false)
  const [showJS, setShowJS] = useState(false)
  const catalogueBlocksTooltip = 'Toggles whether to search the blocks for real cards from the catalogue'
  const artBlocksTooltip = 'Toggles whether to search the blocks for card art'
  const compactBlocksTooltip = 'Render the blocks compactly rather than as always full rectangles'
  const showJSONTooltip = 'Show the JSON representation of the blocks in the workspace'
  const showJSTooltip = 'Show the JS representation of the code in the workspace'

  const catalogueBlocksCheck = useRef(null)
  const catalogueBlocksLabel = useRef(null)
  const artBlocksCheck = useRef(null)
  const artBlocksLabel = useRef(null)
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
    setSearchCatalogueBlocks(!searchCatalogueBlocks)
    setSearchArtBlocks(false)
    workspace().getToolbox().clearSelection()
    if (query.length > 0) {
      search({target: {value: query}})
    }
  }

  const toggleArtBlocks = evt => {
    setSearchArtBlocks(!searchArtBlocks)
    setSearchCatalogueBlocks(false)
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
    addTooltip(artBlocksCheck, artBlocksTooltip)
    addTooltip(artBlocksLabel, artBlocksTooltip)
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
      <Form.Check.Input onChange={e => toggleCatalogueBlocks(e)}
                        checked={searchCatalogueBlocks}
                        className={styles.editorCheck}
                        ref={catalogueBlocksCheck}
      />
      <Form.Check.Label ref={catalogueBlocksLabel}> Search Card Catalogue</Form.Check.Label>
    </Form.Check>
    <Form.Check className={styles.editorOption}>
      <Form.Check.Input onChange={e => toggleArtBlocks(e)}
                        checked={searchArtBlocks}
                        className={styles.editorCheck}
                        ref={artBlocksCheck}
      />
      <Form.Check.Label ref={artBlocksLabel}> Search Card Art</Form.Check.Label>
    </Form.Check>
    <CardEditorWorkspace setJSON={setCode}
                         setJS={setRealCode}
                         searchCatalogueBlocks={searchCatalogueBlocks}
                         searchArtBlocks={searchArtBlocks}
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