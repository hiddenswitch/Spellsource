# Unity UI slices

These PNGs are lossless exports of named layers in the private Unity source's
`Spellsource UI.psd`. They are intentionally small source sprites, not images
to be stretched with `background-size: 100% 100%`.

Use them as CSS nine-slice backgrounds, for example:

```css
.gameButton {
  border: 6px solid transparent;
  border-image: url('/static/game-ui/slices/button-medium-raised.png') 5 5 6 6 fill;
}

.gameButton:active {
  border-image-source: url('/static/game-ui/slices/button-medium-pressed.png');
}
```

The CSS slice order is `top right bottom left`; it is converted from Unity's
`left bottom right top` sprite border convention.

| Asset | Unity layer | Unity border (L B R T) | CSS slice (T R B L) |
| --- | --- | --- | --- |
| `button-medium-flat.png` | Medium Button Flat | 6 6 4 4 | 4 4 6 6 |
| `button-medium-raised.png` | Medium Button Raised | 6 6 5 5 | 5 5 6 6 |
| `button-medium-pressed.png` | Medium Button Pressed | 6 6 5 5 | 5 5 6 6 |
| `button-brand-raised.png` | Brand Button Raised | 6 6 5 5 | 5 5 6 6 |
| `button-brand-pressed.png` | Brand Button Pressed | 6 6 5 5 | 5 5 6 6 |
| `panel-raised.png` | Panel Frame 1px Border Raised | 5 6 4 4 | 4 4 6 5 |
| `panel-opaque.png` | Panel Frame 1px Flat Opaque Shade | 4 4 6 6 | 6 6 4 4 |
| `panel-dark-opaque.png` | Panel Dark Frame 1px Flat Opaque Shade | 4 4 6 6 | 6 6 4 4 |
| `panel-all-elements.png` | Panel Frame 1px All Elements | 5 6 6 6 | 6 6 6 5 |

The source PSD remains private. This directory contains only the web-ready
exports needed by the public site.
