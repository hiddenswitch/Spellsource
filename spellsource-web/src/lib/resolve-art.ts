import { defaultsDeep } from 'lodash/object'

/*module.exports = {
  resolveArt: function resolveArt (source, context) {
    if (source.type === 'CLASS') {
      return source.art
    }

    // if the art field already exists, just extend it
    // with the appropriate class colors
    let heroClass = source.heroClass
    if (!heroClass) {
      return source.art
    }

    // The card JSON uses ANY as the enum to mean a neutral card
    if (heroClass === 'ANY') {
      // the name of the class card is going to be class_neutral not class_any
      heroClass = 'neutral'
    }
    const classCard = context.nodeModel.getNodeById({ id: `class_${heroClass.toLowerCase()}` })
    if (!classCard) {
      return source.art
    }

    const art = source.art || {}

    defaultsDeep(art, classCard.art)
    return art
  }
}*/
