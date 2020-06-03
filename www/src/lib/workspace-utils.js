import { Xml } from 'blockly'
import { extend, filter, find, fromPairs, isArray, map } from 'lodash'
import format from 'string-format'

export default class WorkspaceUtils {
  static BLOCKLY_OPENER = 'BLOCKLY_OPENER'
  static BLOCKLY_AFTERMATH = 'BLOCKLY_AFTERMATH'
  static BLOCKLY_ADD_TARGET_OUTPUT_TO_CHILD_SPELL = 'BLOCKLY_ADD_TARGET_OUTPUT_TO_CHILD_SPELL'
  static BLOCKLY_ADD_EVENT_TARGET_TO_CHILD_SPELL = 'BLOCKLY_ADD_EVENT_TARGET_TO_CHILD_SPELL'
  static BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE = 'BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE'
  static BLOCKLY_INT_ATTRIBUTE = 'BLOCKLY_INT_ATTRIBUTE'
  static BLOCKLY_RANDOM_TARGET = 'BLOCKLY_RANDOM_TARGET'
  static BLOCKLY_ARRAY_ELEMENT = 'BLOCKLY_ARRAY_ELEMENT'
  static BLOCKLY_EXTEND_PREVIOUS = 'BLOCKLY_EXTEND_PREVIOUS'

  /**
   * Process a given piece of XML, returning a "CardScript" JSON token that corresponds to it.
   *
   * When given an XML document, this will descend, depth-first, to recursive calls to this function, building the
   * object bottom-up.
   *
   * For example,
   *
   * <block 1> xmlToDictionary(1) {
   *   <statement 2> xmlToDictionary(2) {
   *     <block 3 ...> xmlToDictionary(3) {return reduce 2, 3}
   *       <next>
   *         <block 4 ...> xmlToDictionary(4) {return reduce 3, 4}
   *       </next>
   *   </statement> return reduce 2, 1 }
   * </block> return 1 }
   *
   * The way we signal the time and objects to reduce with is using the <data> element for blocks.
   *
   * attributes block (something + an attributes object calculated from the crap we received)
   *   int attribute (something + int attribute + bool attribute + int attribute)
   *     bool attribute (something + int attribute + bool attribute)
   *       int attribute add this int attribute to whatever was previous to it (something + int attribute)
   *
   * @param xml
   * @param prev
   * @param parent
   * @returns {{}|*|{}|[]}
   */
  static xmlToCardScript (xml, prev = null, parent = null) {
    let nextNode = null
    let next = null
    switch (xml.nodeName) {
      case '#document':
        if (!!xml.firstElementChild) {
          return WorkspaceUtils.xmlToCardScript(xml.firstElementChild)
        }
        break
      case 'xml':
        if (!!xml.firstElementChild) {
          const elementNodes = filter(Array.from(xml.childNodes), cn => cn.nodeType === Node.ELEMENT_NODE)
          if (elementNodes.length === 1) {
            return WorkspaceUtils.xmlToCardScript(elementNodes[0])
          }
          return map(elementNodes, cn => WorkspaceUtils.xmlToCardScript(cn))
        }
        break
      case 'shadow':
      case 'block':
        const obj = {}
        if (!xml.hasChildNodes()) {
          return obj
        }
        const childNodes = Array.from(xml.childNodes)
        const length = childNodes.length
        for (let i = 0; i < length; i++) {
          const childNode = childNodes[i]
          switch (childNode.nodeName) {
            case '#text':
              // i.e. if childElementCount === 0
              if (length === 1) {
                return xml.innerHTML
              }
              break
            case 'field':
              obj[childNode.attributes['name'].value] = !isNaN(childNode.innerHTML) ? +childNode.innerHTML : childNode.innerHTML
              break
            case 'statement':
            case 'value':
              if (childNode.firstElementChild.nodeName === 'shadow' && childNode.lastElementChild !== childNode.firstElementChild) {
                obj[childNode.attributes['name'].value] = WorkspaceUtils.xmlToCardScript(childNode.lastElementChild, null, obj)
              } else obj[childNode.attributes['name'].value] = WorkspaceUtils.xmlToCardScript(childNode.firstElementChild, null, obj)
              break
            case 'next':
              if (!!childNode.firstElementChild && childNode.firstElementChild.nodeName === 'block') {
                nextNode = childNode.firstElementChild
              }
              break
          }
        }

        if (!!nextNode) {
          next = WorkspaceUtils.xmlToCardScript(nextNode, obj)
        }

        const hasData = find(childNodes, cn => cn.nodeName === 'data')
        if (hasData) {
          const values = hasData.innerHTML.split(',')
          let retValue = null
          for (let i = 0; i < values.length; i++) {
            const value = values[i]
            switch (value) {
              case WorkspaceUtils.BLOCKLY_EXTEND_PREVIOUS:
                if (!!prev) {
                  extend(prev, obj)
                }
                retValue = obj
                break
              case WorkspaceUtils.BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE:
                if (!obj.attribute) {
                  return {}
                }
                const boolAttribute = { [obj.attribute]: true }
                if (!!next) {
                  extend(boolAttribute, next)
                }
                retValue = boolAttribute
                break
              case WorkspaceUtils.BLOCKLY_INT_ATTRIBUTE:
                if (!obj.attribute) {
                  return {}
                }
                const intAttribute = { [obj.attribute]: obj.value }
                if (!!next) {
                  extend(intAttribute, next)
                }
                retValue = intAttribute
                break
              case this.BLOCKLY_ARRAY_ELEMENT:
                // Handle every array statement on this block
                retValue = [obj]
                if (!!obj.i) {
                  retValue = [obj.i]
                }
                if (!!next) {
                  if (isArray(next)) {
                    retValue = retValue.concat(next)
                  } else {
                    retValue = retValue.concat([next])
                  }
                }
                break
              case WorkspaceUtils.BLOCKLY_ADD_TARGET_OUTPUT_TO_CHILD_SPELL:
                if (!!obj.spell && !obj.spell.target) {
                  obj.spell.target = 'OUTPUT'
                }
                break
              case WorkspaceUtils.BLOCKLY_ADD_EVENT_TARGET_TO_CHILD_SPELL:
                if (!!obj.spell && !obj.spell.target) {
                  obj.spell.target = 'EVENT_TARGET'
                }
                break
              case WorkspaceUtils.BLOCKLY_OPENER:
                if (!!prev) {
                  prev['battlecry'] = obj
                }
                retValue = obj
                break
              case WorkspaceUtils.BLOCKLY_AFTERMATH:
                if (!!prev) {
                  prev['deathrattle'] = obj
                }
                retValue = obj
                break
              case WorkspaceUtils.BLOCKLY_RANDOM_TARGET:
                if (!!parent) {
                  parent.randomTarget = true
                }
                retValue = obj['target']
                break
              default:
                const allValues = filter(childNodes, cn =>
                  cn.nodeName === 'field')
                const valuesObj = fromPairs(map(allValues, cn =>
                  [cn.attributes['name'].value, cn.innerHTML])
                )

                const res = format(value, valuesObj)
                retValue = !isNaN(res) ? +res : res
                break
            }
          }
          if (retValue !== null) {
            return retValue
          }
        }
        return obj
      default:
        throw new Error('invalid block type to pass here')
    }
  }

  static workspaceToCardScript (workspace) {
    const xml = Xml.workspaceToDom(workspace)
    console.log(xml)
    return WorkspaceUtils.xmlToCardScript(xml)
  }
}