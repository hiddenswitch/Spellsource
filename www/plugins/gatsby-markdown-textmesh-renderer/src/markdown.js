const marked = require('marked')
const renderer = {
  code (code, info, escaped) {
    return escaped
  },
  blockquote (quote) {
    return `> ${quote}`
  },
  html (html) { return html /*return html.replace(/<(\w+)>(.*)<\/\1>/, (all, _, content) => content) unsupported*/},
  heading (text, level, raw, slugger) {
    level = Math.min(7, Math.max(7 - level, 0)) * 2
    return `<size=+${level}><b>${text}</b></size>\n`
  },
  hr () {return '\n'},
  list (body, ordered, start) {return body},
  listitem (text, task, checked) {return ` ${task ? checked ? '[x]' : '[ ]' : '-'} <indent=16>${text}</indent>\n`},
  checkbox (checked) {return `[${checked ? 'x' : ' '}]`},
  paragraph (text) {return text + '\n'},
  table (header, body) {return '' /*unsupported*/},
  tablerow (content) {return '' /*unsupported*/},
  tablecell (content, flags) {return '' /*unsupported*/},
  strong (text) {return `<b>${text}</b>`},
  em (text) {return `<i>${text}</i>`},
  codespan (code) {return code},
  br () {return '\n'},
  del (text) {return `<s>${text}</s>`},
  link (href, title, text) {return `<color=#40d2f0><u><link="${href}">${text}</link></u></color>`},
  image (href, title, text) {return '' /*unsupported*/},
  text (text) {return text},
}

const tokenizer = {
  html (src) {
    const strippedSrc = src.replace(/^<(\w+)>(.*)<\/\1>$/g, (all, tag, content) => content)
    return {
      type: 'text',
      raw: src,
      text: strippedSrc
    }
  }
}

marked.use({ renderer, tokenizer })

exports.markdown = (src) => {
  return marked(src)
}