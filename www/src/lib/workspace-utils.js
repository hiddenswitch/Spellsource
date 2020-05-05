import { Xml } from 'blockly'
import { find, map, fromPairs, filter } from 'lodash'
import format from 'string-format'

export default class WorkspaceUtils {
  static BLOCKLY_OPENER = 'BLOCKLY_OPENER'

  static xmlToDictionary (xml, prev = null) {
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
        let next = null
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
              obj[childNode.attributes['name'].value] = WorkspaceUtils.xmlToDictionary(childNode.firstElementChild)
              break
            case 'next':
              if (!!childNode.firstElementChild && childNode.firstElementChild.nodeName === 'block') {
                next = childNode.firstElementChild
              }
              break
          }
        }

        if (!!next) {
          WorkspaceUtils.xmlToDictionary(next, obj)
        }

        const hasData = find(childNodes, cn => cn.nodeName === 'data')
        if (hasData) {
          const value = hasData.innerHTML

          switch (value) {
            case WorkspaceUtils.BLOCKLY_OPENER:
              if (!!prev) {
                prev['battlecry'] = obj
              }
              return prev
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