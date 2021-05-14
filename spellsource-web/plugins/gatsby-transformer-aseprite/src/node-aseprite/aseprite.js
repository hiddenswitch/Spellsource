// This is a generated file! Please edit source .ksy file and use kaitai-struct-compiler to rebuild

(function (root, factory) {
  if (typeof define === 'function' && define.amd) {
    define(['kaitai-struct/KaitaiStream'], factory);
  } else if (typeof module === 'object' && module.exports) {
    module.exports = factory(require('kaitai-struct/KaitaiStream'));
  } else {
    root.Aseprite = factory(root.KaitaiStream);
  }
}(this, function (KaitaiStream) {
var Aseprite = (function() {
  function Aseprite(_io, _parent, _root) {
    this._io = _io;
    this._parent = _parent;
    this._root = _root || this;

    this._read();
  }
  Aseprite.prototype._read = function() {
    this.header = new Header(this._io, this, this._root);
    this.frames = new Array(this.header.numFrames);
    for (var i = 0; i < this.header.numFrames; i++) {
      this.frames[i] = new Frame(this._io, this, this._root);
    }
  }

  var Header = Aseprite.Header = (function() {
    Header.PixelFormatEnum = Object.freeze({
      INDEXED: 8,
      GRAYSCALE: 16,
      RGBA: 32,

      8: "INDEXED",
      16: "GRAYSCALE",
      32: "RGBA",
    });

    function Header(_io, _parent, _root) {
      this._io = _io;
      this._parent = _parent;
      this._root = _root || this;

      this._read();
    }
    Header.prototype._read = function() {
      this.fileSize = this._io.readU4le();
      this.magic = this._io.readBytes(2);
      if (!((KaitaiStream.byteArrayCompare(this.magic, [224, 165]) == 0))) {
        throw new KaitaiStream.ValidationNotEqualError([224, 165], this.magic, this._io, "/types/header/seq/1");
      }
      this.numFrames = this._io.readU2le();
      this.width = this._io.readU2le();
      this.height = this._io.readU2le();
      this.pixelFormat = this._io.readU2le();
      this.flags = new FlagsBitset(this._io, this, this._root);
      this.speed = this._io.readU2le();
      this._unnamed8 = this._io.readU8le();
      this.transparentIndex = this._io.readU1();
      this._unnamed10 = this._io.readBitsIntBe(24);
      this._io.alignToByte();
      this.numColors = this._io.readU2le();
      this.pixelWidth = this._io.readU1();
      this.pixelHeight = this._io.readU1();
      this.gridX = this._io.readS2le();
      this.gridY = this._io.readS2le();
      this.gridWidth = this._io.readU2le();
      this.gridHeight = this._io.readU2le();
      this._unnamed18 = this._io.readBytes(84);
    }

    var FlagsBitset = Header.FlagsBitset = (function() {
      function FlagsBitset(_io, _parent, _root) {
        this._io = _io;
        this._parent = _parent;
        this._root = _root || this;

        this._read();
      }
      FlagsBitset.prototype._read = function() {
        this._unnamed0 = this._io.readBitsIntBe(7);
        this.validOpacity = this._io.readBitsIntBe(1) != 0;
        this._unnamed2 = this._io.readBitsIntBe(24);
      }

      /**
       * Layer opacity has a valid value
       */

      return FlagsBitset;
    })();

    /**
     * File size
     */

    /**
     * Magic Number (0xA5E0)
     */

    /**
     * The number of frames in the animation
     */

    /**
     * The width of the sprite, in pixels
     */

    /**
     * The height of the sprite, in pixels
     */

    /**
     * The color depth of the sprite:
     * - "rgba" -> 32bpp, full RGB with alpha (8bpc)
     * - "grayscale" -> 16bpp, value and alpha (8bpc)
     * - "indexed" -> 8bpp indexed
     */

    /**
     * Various boolean flags about the ASE file
     */

    /**
     * DEPRECATED!
     * The speed of the animation (the number of milliseconds
     * between frames).
     * 
     * You should use the frame duration field from each frame
     * header from now on instead of using this field.
     */

    /**
     * Palette entry (index) which represents the transparent
     * color in all non-background layers (only for
     * pixel_format=indexed (8bpp) sprites).
     */

    /**
     * Number of colors (0 means 256 for old sprites).
     */

    /**
     * Per-pixel width (pixel ratio is pixel_width/pixel_height).
     * If this or pixel_height field is zero, pixel ratio is 1:1.
     */

    /**
     * Per-pixel height (pixel ratio is pixel_width/pixel_height).
     * If this or pixel_width field is zero, pixel ratio is 1:1.
     */

    /**
     * The X position of the grid
     */

    /**
     * The Y position of the grid
     */

    /**
     * Grid width (zero if there is no grid, grid size
     * is 16x16 on Aseprite by default)
     */

    /**
     * Grid height (zero if there is no grid, grid size
     * is 16x16 on Aseprite by default)
     */

    return Header;
  })();

  var Frame = Aseprite.Frame = (function() {
    function Frame(_io, _parent, _root) {
      this._io = _io;
      this._parent = _parent;
      this._root = _root || this;

      this._read();
    }
    Frame.prototype._read = function() {
      this.header = new Header(this._io, this, this._root);
      this.chunks = new Array((this.header.numChunks == 0 ? this.header.numChunksOld : this.header.numChunks));
      for (var i = 0; i < (this.header.numChunks == 0 ? this.header.numChunksOld : this.header.numChunks); i++) {
        this.chunks[i] = new Chunk(this._io, this, this._root);
      }
    }

    var Header = Frame.Header = (function() {
      function Header(_io, _parent, _root) {
        this._io = _io;
        this._parent = _parent;
        this._root = _root || this;

        this._read();
      }
      Header.prototype._read = function() {
        this.frameBytes = this._io.readU4le();
        this.magic = this._io.readBytes(2);
        if (!((KaitaiStream.byteArrayCompare(this.magic, [250, 241]) == 0))) {
          throw new KaitaiStream.ValidationNotEqualError([250, 241], this.magic, this._io, "/types/frame/types/header/seq/1");
        }
        this.numChunksOld = this._io.readU2le();
        this.duration = this._io.readU2le();
        this._unnamed4 = this._io.readBytes(2);
        this.numChunks = this._io.readU4le();
      }

      /**
       * Bytes in this frame
       */

      /**
       * Magic number (always 0xF1FA)
       */

      /**
       * Old field which specifies the number of "chunks"
       * in this frame. If this value is 0xFFFF, we might
       * have more chunks to read in this frame.
       */

      /**
       * Frame duration, in milliseconds.
       * 
       * If this value is 0, replace it with the `speed` value
       * from the main ASE header. If you are writing this ASE
       * file back out, be sure to set this field to the `speed` value
       * instead of keeping it as 0, since the `speed` field
       * is deprecated.
       */

      /**
       * New field which specifies the number of "chunks"
       * in this frame (if this is 0, use the num_chunks_old
       * field).
       */

      return Header;
    })();

    var Chunk = Frame.Chunk = (function() {
      Chunk.ChunkTypeEnum = Object.freeze({
        PALETTE_OLD_1: 4,
        PALETTE_OLD_2: 17,
        LAYER: 8196,
        CEL: 8197,
        CEL_EXTRA: 8198,
        COLOR_PROFILE: 8199,
        MASK: 8214,
        PATH: 8215,
        TAGS: 8216,
        PALETTE: 8217,
        USERDATA: 8224,
        SLICE: 8226,

        4: "PALETTE_OLD_1",
        17: "PALETTE_OLD_2",
        8196: "LAYER",
        8197: "CEL",
        8198: "CEL_EXTRA",
        8199: "COLOR_PROFILE",
        8214: "MASK",
        8215: "PATH",
        8216: "TAGS",
        8217: "PALETTE",
        8224: "USERDATA",
        8226: "SLICE",
      });

      function Chunk(_io, _parent, _root) {
        this._io = _io;
        this._parent = _parent;
        this._root = _root || this;

        this._read();
      }
      Chunk.prototype._read = function() {
        this.size = this._io.readU4le();
        this.type = this._io.readU2le();
        switch (this.type) {
        case Aseprite.Frame.Chunk.ChunkTypeEnum.SLICE:
          this.data = new SliceChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.PALETTE:
          this.data = new PaletteChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.CEL:
          this.data = new CelChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.MASK:
          this.data = new MaskChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.USERDATA:
          this.data = new UserdataChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.LAYER:
          this.data = new LayerChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.TAGS:
          this.data = new TagsChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.CEL_EXTRA:
          this.data = new CelExtraChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.PALETTE_OLD_2:
          this.data = new PaletteOldChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.PALETTE_OLD_1:
          this.data = new PaletteOldChunk(this._io, this, this._root);
          break;
        case Aseprite.Frame.Chunk.ChunkTypeEnum.COLOR_PROFILE:
          this.data = new ColorProfileChunk(this._io, this, this._root);
          break;
        default:
          this.data = new DummyChunk(this._io, this, this._root);
          break;
        }
      }

      var TagsChunk = Chunk.TagsChunk = (function() {
        function TagsChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        TagsChunk.prototype._read = function() {
          this.numTags = this._io.readU2le();
          this._unnamed1 = this._io.readBytes(8);
          this.tags = new Array(this.numTags);
          for (var i = 0; i < this.numTags; i++) {
            this.tags[i] = new Tag(this._io, this, this._root);
          }
        }

        var Tag = TagsChunk.Tag = (function() {
          Tag.DirectionEnum = Object.freeze({
            FORWARD: 0,
            REVERSE: 1,
            PING_PONG: 2,

            0: "FORWARD",
            1: "REVERSE",
            2: "PING_PONG",
          });

          function Tag(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          Tag.prototype._read = function() {
            this.from = this._io.readU2le();
            this.to = this._io.readU2le();
            this.direction = this._io.readU1();
            this._unnamed3 = this._io.readBytes(8);
            this.color = new Color(this._io, this, this._root);
            this._unnamed5 = this._io.readU1();
            this.nameSize = this._io.readU2le();
            this.name = KaitaiStream.bytesToStr(this._io.readBytes(this.nameSize), "utf-8");
          }

          var Color = Tag.Color = (function() {
            function Color(_io, _parent, _root) {
              this._io = _io;
              this._parent = _parent;
              this._root = _root || this;

              this._read();
            }
            Color.prototype._read = function() {
              this.r = this._io.readU1();
              this.g = this._io.readU1();
              this.b = this._io.readU1();
            }

            /**
             * The red channel (0-255)
             */

            /**
             * The green channel (0-255)
             */

            /**
             * The blue channel (0-255)
             */

            return Color;
          })();

          /**
           * From frame
           */

          /**
           * To frame
           */

          /**
           * Loop animation direction
           */

          /**
           * The tag's color
           */

          /**
           * The tag's name
           */

          return Tag;
        })();

        /**
         * The number of tags
         */

        /**
         * The sprite's list of tags
         */

        return TagsChunk;
      })();

      var SliceChunk = Chunk.SliceChunk = (function() {
        function SliceChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        SliceChunk.prototype._read = function() {
          this.numKeys = this._io.readU4le();
          this.flags = new FlagsBitset(this._io, this, this._root);
          this._unnamed2 = this._io.readU4le();
          this.nameSize = this._io.readU2le();
          this.name = KaitaiStream.bytesToStr(this._io.readBytes(this.nameSize), "utf-8");
          this.keys = new Array(this.numKeys);
          for (var i = 0; i < this.numKeys; i++) {
            this.keys[i] = new Key(this._io, this, this._root);
          }
        }

        var FlagsBitset = SliceChunk.FlagsBitset = (function() {
          function FlagsBitset(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          FlagsBitset.prototype._read = function() {
            this._unnamed0 = this._io.readBitsIntBe(6);
            this.hasPivot = this._io.readBitsIntBe(1) != 0;
            this.patch9 = this._io.readBitsIntBe(1) != 0;
            this._unnamed3 = this._io.readBitsIntBe(24);
          }

          /**
           * Has pivot information
           */

          /**
           * It's a 9-patches slice
           */

          return FlagsBitset;
        })();

        var Key = SliceChunk.Key = (function() {
          function Key(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          Key.prototype._read = function() {
            this.frameNo = this._io.readU4le();
            this.x = this._io.readS4le();
            this.y = this._io.readS4le();
            this.width = this._io.readU4le();
            this.height = this._io.readU4le();
            if (this._parent.flags.patch9 == true) {
              this.centerX = this._io.readS4le();
            }
            if (this._parent.flags.patch9 == true) {
              this.centerY = this._io.readS4le();
            }
            if (this._parent.flags.patch9 == true) {
              this.centerWidth = this._io.readU4le();
            }
            if (this._parent.flags.patch9 == true) {
              this.centerHeight = this._io.readU4le();
            }
            if (this._parent.flags.hasPivot == true) {
              this.pivotX = this._io.readS4le();
            }
            if (this._parent.flags.hasPivot == true) {
              this.pivotY = this._io.readS4le();
            }
          }

          /**
           * Frame number (this slice is valid from thie frame to
           * the end of the animation)
           */

          /**
           * Slice X origin coordinate in the sprite
           */

          /**
           * Slice Y origin coordinate in the sprite
           */

          /**
           * Slice width (can be 0 if this slice is hidden
           * in the animation from the given frame)
           */

          /**
           * Slice height (can be 0 if this slice is hidden
           * in the animation from the given frame)
           */

          /**
           * Center X position (relative to slice bounds)
           */

          /**
           * Center Y position (relative to slice bounds)
           */

          /**
           * Center width
           */

          /**
           * Center height
           */

          /**
           * Pivot X position (relative to the slice origin)
           */

          /**
           * Pivot y position (relative to the slice origin)
           */

          return Key;
        })();

        /**
         * Number of "slice keys"
         */

        /**
         * Slice flags
         */

        /**
         * The slice's name
         */

        /**
         * The slice's list of keys
         */

        return SliceChunk;
      })();

      var DummyChunk = Chunk.DummyChunk = (function() {
        function DummyChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        DummyChunk.prototype._read = function() {
          this._unnamed0 = this._io.readBytes((this._parent.size - 6));
        }

        return DummyChunk;
      })();

      var MaskChunk = Chunk.MaskChunk = (function() {
        function MaskChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        MaskChunk.prototype._read = function() {
          this.x = this._io.readS2le();
          this.y = this._io.readS2le();
          this.width = this._io.readU2le();
          this.height = this._io.readU2le();
          this._unnamed4 = this._io.readBytes(8);
          this.nameSize = this._io.readU2le();
          this.name = KaitaiStream.bytesToStr(this._io.readBytes(this.nameSize), "utf-8");
          this.bitmap = this._io.readBytes((this.height * Math.floor((this.width + 7) / 8)));
        }

        /**
         * X position
         */

        /**
         * Y position
         */

        /**
         * Mask width
         */

        /**
         * Mask height
         */

        /**
         * Mask name
         */

        /**
         * Each byte contains 8 pixels (the leftmost pixels
         * are packed into the high order bits)
         */

        return MaskChunk;
      })();

      var LayerChunk = Chunk.LayerChunk = (function() {
        LayerChunk.TypeEnum = Object.freeze({
          IMAGE: 0,
          GROUP: 1,

          0: "IMAGE",
          1: "GROUP",
        });

        LayerChunk.BlendModeEnum = Object.freeze({
          NORMAL: 0,
          MULTIPLY: 1,
          SCREEN: 2,
          OVERLAY: 3,
          DARKEN: 4,
          LIGHTEN: 5,
          COLOR_DODGE: 6,
          COLOR_BURN: 7,
          HARD_LIGHT: 8,
          SOFT_LIGHT: 9,
          DIFFERENCE: 10,
          EXCLUSION: 11,
          HUE: 12,
          SATURATION: 13,
          COLOR: 14,
          LUMINOSITY: 15,
          ADDITION: 16,
          SUBTRACT: 17,
          DIVIDE: 18,

          0: "NORMAL",
          1: "MULTIPLY",
          2: "SCREEN",
          3: "OVERLAY",
          4: "DARKEN",
          5: "LIGHTEN",
          6: "COLOR_DODGE",
          7: "COLOR_BURN",
          8: "HARD_LIGHT",
          9: "SOFT_LIGHT",
          10: "DIFFERENCE",
          11: "EXCLUSION",
          12: "HUE",
          13: "SATURATION",
          14: "COLOR",
          15: "LUMINOSITY",
          16: "ADDITION",
          17: "SUBTRACT",
          18: "DIVIDE",
        });

        function LayerChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        LayerChunk.prototype._read = function() {
          this.flags = new FlagBitset(this._io, this, this._root);
          this.type = this._io.readU2le();
          this.childLevel = this._io.readU2le();
          this.layerWidth = this._io.readU2le();
          this.layerHeight = this._io.readU2le();
          this.blendMode = this._io.readU2le();
          this.opacity = this._io.readU1();
          this._unnamed7 = this._io.readBytes(3);
          this.nameSize = this._io.readU2le();
          this.name = KaitaiStream.bytesToStr(this._io.readBytes(this.nameSize), "utf-8");
        }

        var FlagBitset = LayerChunk.FlagBitset = (function() {
          function FlagBitset(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          FlagBitset.prototype._read = function() {
            this._unnamed0 = this._io.readBitsIntBe(1) != 0;
            this.reference = this._io.readBitsIntBe(1) != 0;
            this.collapsed = this._io.readBitsIntBe(1) != 0;
            this.preferLinked = this._io.readBitsIntBe(1) != 0;
            this.background = this._io.readBitsIntBe(1) != 0;
            this.lockMovement = this._io.readBitsIntBe(1) != 0;
            this.editable = this._io.readBitsIntBe(1) != 0;
            this.visible = this._io.readBitsIntBe(1) != 0;
            this._io.alignToByte();
            this._unnamed8 = this._io.readU1();
          }

          /**
           * The layer is a reference layer
           */

          /**
           * The layer group should be displayed collapsed
           */

          /**
           * Prefer linked cels
           */

          /**
           * Layer is a background
           */

          /**
           * Lock any movement on the layer
           */

          /**
           * Layer is editable
           */

          /**
           * Layer is visible
           */

          return FlagBitset;
        })();

        /**
         * Layer flags
         */

        /**
         * Layer type
         */

        /**
         * Layer child level
         * 
         * The child level is used to show the relationship of this
         * layer with the last one read, for example:
         * 
         *     Layer name and hierarchy      Child Level
         *     -----------------------------------------------
         *     - Background                  0
         *       `- Layer1                   1
         *     - Foreground                  0
         *       |- My set1                  1
         *       |  `- Layer2                2
         *       `- Layer3                   1
         */

        /**
         * IGNORED. Default layer width in pixels.
         */

        /**
         * IGNORED. Default layer height in pixels.
         */

        /**
         * The layer blend mode (always 0 for group layers)
         */

        /**
         * Layer opacity (0-255)
         * NOTE: Only valid if flags.visible==true.
         */

        /**
         * Layer name
         */

        return LayerChunk;
      })();

      var CelChunk = Chunk.CelChunk = (function() {
        CelChunk.CelTypeEnum = Object.freeze({
          RAW: 0,
          LINKED: 1,
          COMPRESSED: 2,

          0: "RAW",
          1: "LINKED",
          2: "COMPRESSED",
        });

        function CelChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        CelChunk.prototype._read = function() {
          this.layerIndex = this._io.readU2le();
          this.x = this._io.readS2le();
          this.y = this._io.readS2le();
          this.opacity = this._io.readU1();
          this.type = this._io.readU2le();
          this._unnamed5 = this._io.readBytes(7);
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.RAW) {
            this.rawWidth = this._io.readU2le();
          }
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.RAW) {
            this.rawHeight = this._io.readU2le();
          }
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.RAW) {
            this.pixels = this._io.readBytes((this._parent.size - 26));
          }
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.LINKED) {
            this.frameLink = this._io.readU2le();
          }
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.COMPRESSED) {
            this.width = this._io.readU2le();
          }
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.COMPRESSED) {
            this.height = this._io.readU2le();
          }
          if (this.type == Aseprite.Frame.Chunk.CelChunk.CelTypeEnum.COMPRESSED) {
            this.pixelsCompressed = this._io.readBytes((this._parent.size - 26));
          }
        }

        /**
         * Layer index
         * 
         * The layer index is a number to identify any layer in the
         * sprite, for example:
         * 
         *     Layer name and hierarchy      Layer index
         *     -----------------------------------------------
         *     - Background                  0
         *       `- Layer1                   1
         *     - Foreground                  2
         *       |- My set1                  3
         *       |  `- Layer2                4
         *       `- Layer3                   5
         */

        /**
         * X position
         */

        /**
         * Y position
         */

        /**
         * Opacity level (0-255)
         */

        /**
         * The format of the cel's contents
         */

        /**
         * The cel width in pixels
         */

        /**
         * The cel height in pixels
         */

        /**
         * The cel's pixel data
         * 
         * NOTE: This is raw byte data because Kaitai cannot accurately
         * represent the variable types here. You will need to check
         * the color_profile layer to check which format these pixels
         * will be in:
         * 
         * - rgba == 4 bytes
         * - grayscale == 2 bytes
         * - indexed = 1 byte
         * 
         * Pixels are read row by row from top to bottom,
         * for each scanline read pixels from left to right.
         */

        /**
         * The frame position to link with
         */

        /**
         * The width in pixels
         */

        /**
         * The height in pixels
         */

        /**
         * The compressed pixel data. Must be inflated (Kaitai does not
         * support this out of the box).
         * 
         * NOTE: This is raw byte data because Kaitai cannot accurately
         * represent the variable types here. You will need to check
         * the color_profile layer to check which format these pixels
         * will be in:
         * 
         * - rgba == 4 bytes
         * - grayscale == 2 bytes
         * - indexed = 1 byte
         * 
         * Pixels are read row by row from top to bottom,
         * for each scanline read pixels from left to right.
         */

        return CelChunk;
      })();

      var PaletteOldChunk = Chunk.PaletteOldChunk = (function() {
        function PaletteOldChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        PaletteOldChunk.prototype._read = function() {
          this.numPackets = this._io.readU2le();
          this.packets = new Array(this.numPackets);
          for (var i = 0; i < this.numPackets; i++) {
            this.packets[i] = new Packet(this._io, this, this._root);
          }
        }

        var Packet = PaletteOldChunk.Packet = (function() {
          function Packet(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          Packet.prototype._read = function() {
            this.skip = this._io.readU1();
            this.numColors = this._io.readU1();
            this.colors = new Array((this.numColors == 0 ? 256 : this.numColors));
            for (var i = 0; i < (this.numColors == 0 ? 256 : this.numColors); i++) {
              this.colors[i] = new Color(this._io, this, this._root);
            }
          }

          var Color = Packet.Color = (function() {
            function Color(_io, _parent, _root) {
              this._io = _io;
              this._parent = _parent;
              this._root = _root || this;

              this._read();
            }
            Color.prototype._read = function() {
              this.r = this._io.readU1();
              this.g = this._io.readU1();
              this.b = this._io.readU1();
            }

            /**
             * Red channel (0-255)
             */

            /**
             * Green channel (0-255)
             */

            /**
             * Blue channel (0-255)
             */

            return Color;
          })();

          /**
           * The number of palette entries to skip from the last
           * packet (start from 0)
           */

          /**
           * The number of colors in the packet (0 means 256)
           */

          /**
           * The packet's colors
           */

          return Packet;
        })();

        /**
         * Number of packets
         */

        /**
         * The chunk's packets
         */

        return PaletteOldChunk;
      })();

      var PaletteChunk = Chunk.PaletteChunk = (function() {
        function PaletteChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        PaletteChunk.prototype._read = function() {
          this.numEntries = this._io.readU4le();
          this.firstIndex = this._io.readU4le();
          this.lastIndex = this._io.readU4le();
          this._unnamed3 = this._io.readBytes(8);
          this.entries = new Array(this.numEntries);
          for (var i = 0; i < this.numEntries; i++) {
            this.entries[i] = new Entry(this._io, this, this._root);
          }
        }

        var Entry = PaletteChunk.Entry = (function() {
          function Entry(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          Entry.prototype._read = function() {
            this.flags = new FlagBitset(this._io, this, this._root);
            this.r = this._io.readU1();
            this.g = this._io.readU1();
            this.b = this._io.readU1();
            this.a = this._io.readU1();
            if (this.flags.hasName == true) {
              this.nameLength = this._io.readU2le();
            }
            if (this.flags.hasName == true) {
              this.name = KaitaiStream.bytesToStr(this._io.readBytes(this.nameLength), "utf-8");
            }
          }

          var FlagBitset = Entry.FlagBitset = (function() {
            function FlagBitset(_io, _parent, _root) {
              this._io = _io;
              this._parent = _parent;
              this._root = _root || this;

              this._read();
            }
            FlagBitset.prototype._read = function() {
              this._unnamed0 = this._io.readBitsIntBe(7);
              this.hasName = this._io.readBitsIntBe(1) != 0;
              this._unnamed2 = this._io.readBitsIntBe(8);
            }

            return FlagBitset;
          })();

          /**
           * Entry flags
           */

          /**
           * Red channel (0-255)
           */

          /**
           * Green channel (0-255)
           */

          /**
           * Blue channel (0-255)
           */

          /**
           * Alpha channel (0-255)
           */

          /**
           * The name of the palette entry
           */

          return Entry;
        })();

        /**
         * New palette size (total number of entries)
         */

        /**
         * First color index to change
         */

        /**
         * Last color index to change
         */

        /**
         * The palette entries
         */

        return PaletteChunk;
      })();

      var UserdataChunk = Chunk.UserdataChunk = (function() {
        function UserdataChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        UserdataChunk.prototype._read = function() {
          this.flags = new FlagsBitset(this._io, this, this._root);
          if (this.flags.hasText == true) {
            this.textSize = this._io.readU2le();
          }
          if (this.flags.hasText == true) {
            this.text = KaitaiStream.bytesToStr(this._io.readBytes(this.textSize), "utf-8");
          }
          if (this.flags.hasColor == true) {
            this.r = this._io.readU1();
          }
          if (this.flags.hasColor == true) {
            this.g = this._io.readU1();
          }
          if (this.flags.hasColor == true) {
            this.b = this._io.readU1();
          }
          if (this.flags.hasColor == true) {
            this.a = this._io.readU1();
          }
        }

        var FlagsBitset = UserdataChunk.FlagsBitset = (function() {
          function FlagsBitset(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          FlagsBitset.prototype._read = function() {
            this._unnamed0 = this._io.readBitsIntBe(6);
            this.hasColor = this._io.readBitsIntBe(1) != 0;
            this.hasText = this._io.readBitsIntBe(1) != 0;
            this._unnamed3 = this._io.readBitsIntBe(24);
          }

          /**
           * Userdata has color information
           */

          /**
           * Userdata has textual information
           */

          return FlagsBitset;
        })();

        /**
         * Flags for the userdata
         */

        /**
         * Userdata text
         */

        /**
         * The red channel (0-255)
         */

        /**
         * The green channel (0-255)
         */

        /**
         * The blue channel (0-255)
         */

        /**
         * The alpha channel (0-255)
         */

        return UserdataChunk;
      })();

      var ColorProfileChunk = Chunk.ColorProfileChunk = (function() {
        ColorProfileChunk.TypeEnum = Object.freeze({
          NONE: 0,
          SRGB: 1,
          EMBEDDED_ICC: 2,

          0: "NONE",
          1: "SRGB",
          2: "EMBEDDED_ICC",
        });

        function ColorProfileChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        ColorProfileChunk.prototype._read = function() {
          this.type = this._io.readU2le();
          this.flags = new Flags(this._io, this, this._root);
          this.fixedGamma = new FixedFloat(this._io, this, this._root);
          this._unnamed3 = this._io.readBytes(8);
          if (this.type == Aseprite.Frame.Chunk.ColorProfileChunk.TypeEnum.EMBEDDED_ICC) {
            this.iccLength = this._io.readU4le();
          }
          if (this.type == Aseprite.Frame.Chunk.ColorProfileChunk.TypeEnum.EMBEDDED_ICC) {
            this.iccData = this._io.readBytes(this.iccLength);
          }
        }

        var Flags = ColorProfileChunk.Flags = (function() {
          function Flags(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          Flags.prototype._read = function() {
            this._unnamed0 = this._io.readBitsIntBe(7);
            this.fixedGamma = this._io.readBitsIntBe(1) != 0;
            this._unnamed2 = this._io.readBitsIntBe(8);
          }

          /**
           * Use special fixed gamma
           */

          return Flags;
        })();

        var FixedFloat = ColorProfileChunk.FixedFloat = (function() {
          function FixedFloat(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          FixedFloat.prototype._read = function() {
            this.int = this._io.readU2le();
            this.dec = this._io.readU2le();
          }

          /**
           * The integer part of the fixed float
           */

          /**
           * The decimal part of the fixed float
           */

          return FixedFloat;
        })();

        /**
         * The color profile type
         */

        /**
         * Fixed gamma (1.0 = linear)
         * Note: The gamma in sRGB is 2.2 in overall but it doesn't use
         * this fixed gamma, because sRGB uses different gamma sections
         * (linear and non-linear). If sRGB is specified with a fixed
         * gamma = 1.0, it means that this is Linear sRGB.
         */

        /**
         * ICC profile data length
         */

        /**
         * ICC profile data. More info: http://www.color.org/ICC1V42.pdf
         */

        return ColorProfileChunk;
      })();

      var CelExtraChunk = Chunk.CelExtraChunk = (function() {
        function CelExtraChunk(_io, _parent, _root) {
          this._io = _io;
          this._parent = _parent;
          this._root = _root || this;

          this._read();
        }
        CelExtraChunk.prototype._read = function() {
          this.flags = new FlagsBitset(this._io, this, this._root);
          this.preciseX = new Fixed(this._io, this, this._root);
          this.preciseY = new Fixed(this._io, this, this._root);
          this.width = new Fixed(this._io, this, this._root);
          this.height = new Fixed(this._io, this, this._root);
          this._unnamed5 = this._io.readBytes(16);
        }

        var FlagsBitset = CelExtraChunk.FlagsBitset = (function() {
          function FlagsBitset(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          FlagsBitset.prototype._read = function() {
            this._unnamed0 = this._io.readBitsIntBe(7);
            this.preciseBounds = this._io.readBitsIntBe(1) != 0;
          }

          /**
           * Precise bounds are set
           */

          return FlagsBitset;
        })();

        var Fixed = CelExtraChunk.Fixed = (function() {
          function Fixed(_io, _parent, _root) {
            this._io = _io;
            this._parent = _parent;
            this._root = _root || this;

            this._read();
          }
          Fixed.prototype._read = function() {
            this.int = this._io.readU2le();
            this.dec = this._io.readU2le();
          }

          /**
           * The integer part of the fixed float
           */

          /**
           * The decimal part of the fixed float
           */

          return Fixed;
        })();

        /**
         * Flags (set to zero)
         */

        /**
         * Precise X position
         */

        /**
         * Precise Y position
         */

        /**
         * Width of the cel in the sprite (scaled in real-time)
         */

        /**
         * Height of the cel in the sprite (scaled in real-time)
         */

        return CelExtraChunk;
      })();

      /**
       * The size, in bytes, of the chunk (including
       * .size and .type fields).
       */

      /**
       * The chunk type
       */

      return Chunk;
    })();

    return Frame;
  })();

  return Aseprite;
})();
return Aseprite;
}));
