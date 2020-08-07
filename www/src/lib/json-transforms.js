function isString (str) {
  return typeof str === 'string'
}

function isNumber (num) {
  return typeof num == 'number'
}

function isArray (arr) {
  return Array.isArray(arr)
}

module.exports = {
  jsonType: function (object) {
    if (object.hasOwnProperty('fileFormatVersion')) {
      return 'Card'
    } else if ((object.hasOwnProperty('args') || object.hasOwnProperty('messages')) && object.hasOwnProperty('type')) {
      // this is a blockly block
      return 'Block'
    } else if (object.hasOwnProperty('Style') && object.hasOwnProperty('BlockCategoryList')) {
      // this is a toolbox definition
      return 'Toolbox'
    }
    return null
  },
  jsonTransformFileNode: function (object, fileNode) {
    // this is a card JSON
    if (object.hasOwnProperty('fileFormatVersion')) {
      if (!object.id) {
        // Set the id
        object.id = fileNode.base.replace(/.json$/, '')
      }
      // Also set a path on the cards node which corresponds to its URL in the website
      object.path = '/cards/' + object.id
    } else if ((object.hasOwnProperty('args0') || object.hasOwnProperty('message0')) && object.hasOwnProperty('type')) {
      // this is a blockly block
      if (!object.id && !!object.type) {
        object.id = object.type
      }

      const newArgs = []
      // Patch up types
      for (let i = 0; i <= 9; i++) {
        if (!!object['args' + i.toString()]) {
          const args = object['args' + i.toString()]
          args.forEach(arg => {
            if (arg.hasOwnProperty('value')) {
              if (isNumber(arg.value)) {
                arg['valueI'] = arg.value
              } else if (isString(arg.value)) {
                arg['valueS'] = arg.value
              } else if (arg.value === true) {
                arg['valueB'] = true
              } else if (arg.value === false) {
                arg['valueB'] = false
              }
              delete arg.value
            }
            if (!!arg.check) {
              if (isArray(arg.check)) {
                return
              }

              arg['check'] = [arg.check]
            }
          })
          newArgs.push({ i: i, args: args })
          delete object['args' + i.toString()]
        } else {
          break
        }
      }
      const newMessages = []
      for (let i = 0; i <= 9; i++) {
        if (!!object['message' + i.toString()]) {
          let newMessage = object['message' + i.toString()]
          if (!!newArgs[i] && !!newArgs[i].args) {
            for (let j = 0; j < newArgs[i].args.length; j++) {
              let arg = newArgs[i].args[j]
              let token = '%' + (1 + j).toString()
              if (arg.type === 'field_label_serializable_hidden'
                && !newMessage.includes(token)) {
                newMessage = token + newMessage
                //console.warn('Block ' + object.type + ' forgot arg ' + token + ' in message' + i.toString())
              }
            }
          }
          newMessages.push(newMessage)
        } else {
          break
        }
        delete object['message' + i.toString()]
      }
      object.args = newArgs
      object.messages = newMessages
      object.path = '/blocks/' + object.id
    } else if (object.hasOwnProperty('Style') && object.hasOwnProperty('BlockCategoryList')) {
      // this is a toolbox definition
      if (!object.id && !!object.Style) {
        object.id = object.Style
      }
      object.path = '/toolboxes/' + object.id
    }
  }
}