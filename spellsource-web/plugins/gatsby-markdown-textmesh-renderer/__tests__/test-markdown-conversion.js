import { markdown } from '../src/markdown'

describe('markdown', () => {
  test('it converts list elements successfully', () => {
    const input = '### Current Version 0.8.89-3.3.7 (September 2, 2020)\n' +
      '\n' +
      ' - The new large card art now appears on hovers / inspections.\n' +
      ' - Hover / inspection positioning is now improved on devices. Left handed users will soon be given a preferences option to flip the positioning of the inspected card.\n' +
      ' - Rarities are now shown on the large card.'
    const output = '<size=+8><b>Current Version 0.8.89-3.3.7 (September 2, 2020)</b></size>\n' +
      ' - <indent=16>The new large card art now appears on hovers / inspections.</indent>\n' +
      ' - <indent=16>Hover / inspection positioning is now improved on devices. Left handed users will soon be given a preferences option to flip the positioning of the inspected card.</indent>\n' +
      ' - <indent=16>Rarities are now shown on the large card.</indent>\n'

    expect(markdown(input)).toEqual(output)
  })

  test('html content is not removed fully', () => {
    const input = '<summary>Improvements to visualization of spells and hand tray on mobile.</summary>'
    const output = 'Improvements to visualization of spells and hand tray on mobile.'
    expect(markdown(input)).toEqual(output)
  })

  test('html content is still parsed', () => {
    const input = '<summary>Improvements **to**</summary>'
    const output = 'Improvements <b>to</b>\n'
    const result = markdown(input)
    expect(result).toEqual(output)
  })
})