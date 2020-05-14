import { Xml } from 'blockly'
import { find, map, fromPairs, filter, extend } from 'lodash'
import format from 'string-format'

export default class WorkspaceUtils {
  static BLOCKLY_OPENER = 'BLOCKLY_OPENER'
  static BLOCKLY_ADD_TARGET_OUTPUT_TO_CHILD_SPELL = 'BLOCKLY_ADD_TARGET_OUTPUT_TO_CHILD_SPELL'
  static BLOCKLY_ATTRIBUTES = 'BLOCKLY_ATTRIBUTES'
  static BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE = 'BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE'
  static BLOCKLY_INT_ATTRIBUTE = 'BLOCKLY_INT_ATTRIBUTE'
  static BLOCKLY_RANDOM_TARGET = 'BLOCKLY_RANDOM_TARGET'

  static xmlToDictionary (xml, prev = null, parent = null) {
    const statements = []
    let nextNode = null
    let next = null
    switch (xml.nodeName) {
      case '#document':
        if (!!xml.firstElementChild) {
          return WorkspaceUtils.xmlToDictionary(xml.firstElementChild)
        }
        break
      case 'xml':
        if (!!xml.firstElementChild) {
          return WorkspaceUtils.xmlToDictionary(xml.firstElementChild)
        }
        break
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
            case 'value':
              obj[childNode.attributes['name'].value] = WorkspaceUtils.xmlToDictionary(childNode.firstElementChild, null, obj)
              break
            case 'statement':
              statements.push(childNode)
              break
            case 'next':
              if (!!childNode.firstElementChild && childNode.firstElementChild.nodeName === 'block') {
                nextNode = childNode.firstElementChild
              }
              break
          }
        }

        if (!!nextNode) {
          next = WorkspaceUtils.xmlToDictionary(nextNode, obj)
        }

        const hasData = find(childNodes, cn => cn.nodeName === 'data')
        if (hasData) {
          const values = hasData.innerHTML.split(',')

          for (let i = 0; i < values.length; i++) {
            const value = values[i]
            switch (value) {
              case WorkspaceUtils.BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE:
                if (!obj.attribute) {
                  return {}
                }
                const boolAttribute = { [obj.attribute]: true }
                if (!!next) {
                  extend(boolAttribute, next)
                }
                return boolAttribute
              case WorkspaceUtils.BLOCKLY_INT_ATTRIBUTE:
                if (!obj.attribute) {
                  return {}
                }
                const intAttribute = { [obj.attribute]: obj.value }
                if (!!next) {
                  extend(intAttribute, next)
                }
                return intAttribute
              case WorkspaceUtils.BLOCKLY_ATTRIBUTES:
                let attributes = {}
                for (let i = 0; i < statements.length; i++) {
                  const statement = statements[i].firstElementChild
                  if (!statement) {
                    continue
                  }
                  extend(attributes, WorkspaceUtils.xmlToDictionary(statement, {}))
                }
                // Assign the attributes on the "previous" object i.e. the card
                if (!!prev) {
                  prev['attributes'] = attributes
                }

                return attributes
              case WorkspaceUtils.BLOCKLY_ADD_TARGET_OUTPUT_TO_CHILD_SPELL:
                if (!!obj.spell && !obj.spell.target) {
                  obj.spell.target = 'OUTPUT'
                }
                break
              case WorkspaceUtils.BLOCKLY_OPENER:
                if (!!prev) {
                  prev['battlecry'] = obj
                }
                return obj
              case WorkspaceUtils.BLOCKLY_RANDOM_TARGET:
                if (!!parent) {
                  parent.randomTarget = true;
                }
                return obj['target']
              default:
                const allValues = filter(childNodes, cn =>
                  cn.nodeName === 'field')
                const valuesObj = fromPairs(map(allValues, cn =>
                  [cn.attributes['name'].value, cn.innerHTML])
                )

                const res = format(value, valuesObj)
                return !isNaN(res) ? +res : res
            }
          }

        }
        return obj
      default:
        throw new Error('invalid block type to pass here')
    }
  }

  static workspaceToDictionary (workspace) {
    const xml = Xml.workspaceToDom(workspace)
    console.log(xml)
    return WorkspaceUtils.xmlToDictionary(xml)
  }
}