import fs from "fs";
import path from "path";
import zlib from "zlib";
import sharp from "sharp";

export type AseImportType = "Sprite" | "Tileset" | "LayerToSprite";

export interface AseImportedSprite {
  /** Logical sprite name matching Unity AseFileImporter.ImportSprites */
  name: string;
  /** Source .ase / .aseprite file stem */
  sourceFileName: string;
  /** Frame index (0-based) */
  frameIndex: number;
  /** Width in art pixels */
  width: number;
  /** Height in art pixels */
  height: number;
  /** X offset in frame/atlas (top-left origin in Aseprite) */
  x: number;
  /** Y offset in frame/atlas (top-left origin in Aseprite) */
  y: number;
  /** Unity bottom-left rect Y in frame coordinates */
  unityRectY: number;
  /** Normalized pivot (default { x: 0.5, y: 0.5 }) */
  pivot: { x: number; y: number };
  /** 9-slice border { left, bottom, right, top } in pixels */
  border: { left: number; bottom: number; right: number; top: number };
  /** Pixels per unit (Spellsource standard: 50) */
  pixelsPerUnit: number;
  /** Frame duration in milliseconds */
  durationMs: number;
  /** Optional tag name if derived from a FrameTag */
  tagName?: string;
  /** Optional slice name if derived from a SliceChunk */
  sliceName?: string;
  /** Raw RGBA pixel buffer (width * height * 4 bytes, top-to-bottom row order) */
  rgba: Buffer;
}

export interface AseAnimationClipSpec {
  name: string;
  tagName: string;
  frameFrom: number;
  frameTo: number;
  loopDirection: "Forward" | "Reverse" | "PingPong";
  loopTime: boolean;
  spriteNames: string[];
  frameDurationsMs: number[];
}

export interface AseImportResult {
  fileName: string;
  width: number;
  height: number;
  frameCount: number;
  pixelsPerUnit: number;
  importType: AseImportType;
  layers: { name: string; visible: boolean; opacity: number; blendMode: number; isGroup: boolean; childLevel: number }[];
  tags: { name: string; from: number; to: number; loopDirection: "Forward" | "Reverse" | "PingPong" }[];
  slices: {
    name: string;
    keys: {
      frameNumber: number;
      x: number;
      y: number;
      width: number;
      height: number;
      pivot?: { x: number; y: number };
      center?: { x: number; y: number; width: number; height: number };
    }[];
  }[];
  sprites: AseImportedSprite[];
  animations: AseAnimationClipSpec[];
}

interface InternalLayer {
  flags: number;
  layerType: number;
  childLevel: number;
  blendMode: number;
  opacity: number;
  name: string;
  visible: boolean;
}

interface InternalCel {
  layerIndex: number;
  x: number;
  y: number;
  opacity: number;
  celType: number;
  linkedFrame?: number;
  width: number;
  height: number;
  rawPixels?: Buffer;
}

interface InternalTag {
  from: number;
  to: number;
  loopDirection: "Forward" | "Reverse" | "PingPong";
  name: string;
}

interface InternalSliceKey {
  frameNumber: number;
  x: number;
  y: number;
  width: number;
  height: number;
  center?: { x: number; y: number; width: number; height: number };
  pivot?: { x: number; y: number };
}

interface InternalSlice {
  name: string;
  flags: number;
  keys: InternalSliceKey[];
}

const CHUNK_OLD_PALETTE = 0x0004;
const CHUNK_OLD_PALETTE2 = 0x0011;
const CHUNK_LAYER = 0x2004;
const CHUNK_CEL = 0x2005;
const CHUNK_FRAME_TAGS = 0x2018;
const CHUNK_PALETTE = 0x2019;
const CHUNK_SLICE = 0x2022;

/**
 * Parallel TypeScript implementation of Unity's `AseFileImporter` (`Packages/io.tinu.asepriteimporter@1.0.1`).
 * Parses `.ase` / `.aseprite` binary files directly and produces identical sprite names, slices, frame tags,
 * layer compositions, and animation clips at 50 PPU.
 */
export function parseAsepriteBuffer(
  buffer: Buffer,
  options?: {
    fileName?: string;
    importType?: AseImportType;
    pixelsPerUnit?: number;
    defaultPivot?: { x: number; y: number };
    tileSize?: { x: number; y: number };
  }
): AseImportResult {
  const fileName = options?.fileName ?? "sprite";
  const importType: AseImportType = options?.importType ?? "Sprite";
  const pixelsPerUnit = options?.pixelsPerUnit ?? 50;
  const defaultPivot = options?.defaultPivot ?? { x: 0.5, y: 0.5 };

  if (buffer.length < 128) {
    throw new Error(`Invalid Aseprite file (${fileName}): header shorter than 128 bytes`);
  }

  const magic = buffer.readUInt16LE(4);
  if (magic !== 0xa5e0) {
    throw new Error(`Invalid Aseprite magic number in ${fileName}: 0x${magic.toString(16)}`);
  }

  const frameCount = buffer.readUInt16LE(6);
  const canvasWidth = buffer.readUInt16LE(8);
  const canvasHeight = buffer.readUInt16LE(10);
  const colorDepth = buffer.readUInt16LE(12);
  const transparentPaletteIndex = buffer.readUInt8(28);

  const palette: [number, number, number, number][] = Array.from({ length: 256 }, () => [0, 0, 0, 255]);
  const layers: InternalLayer[] = [];
  const tags: InternalTag[] = [];
  const slices: InternalSlice[] = [];
  const frameDurations: number[] = [];
  const frameCels: InternalCel[][] = [];

  let offset = 128;
  for (let f = 0; f < frameCount && offset + 16 <= buffer.length; f++) {
    const frameBytes = buffer.readUInt32LE(offset);
    const frameMagic = buffer.readUInt16LE(offset + 4);
    if (frameMagic !== 0xf1fa) {
      throw new Error(`Invalid Aseprite frame magic at frame ${f} in ${fileName}`);
    }
    const oldChunks = buffer.readUInt16LE(offset + 6);
    const durationMs = buffer.readUInt16LE(offset + 8);
    const newChunks = buffer.readUInt32LE(offset + 12);
    const numChunks = newChunks !== 0 ? newChunks : oldChunks;

    frameDurations.push(durationMs > 0 ? durationMs : 100);
    const celsThisFrame: InternalCel[] = [];

    let chunkOffset = offset + 16;
    for (let c = 0; c < numChunks && chunkOffset + 6 <= offset + frameBytes; c++) {
      const chunkLength = buffer.readUInt32LE(chunkOffset);
      const chunkType = buffer.readUInt16LE(chunkOffset + 4);
      const dataStart = chunkOffset + 6;
      const dataEnd = chunkOffset + chunkLength;

      if (chunkType === CHUNK_LAYER) {
        const flags = buffer.readUInt16LE(dataStart);
        const layerType = buffer.readUInt16LE(dataStart + 2);
        const childLevel = buffer.readUInt16LE(dataStart + 4);
        const blendMode = buffer.readUInt16LE(dataStart + 10);
        const opacity = buffer.readUInt8(dataStart + 12);
        const nameLen = buffer.readUInt16LE(dataStart + 16);
        const name = buffer.subarray(dataStart + 18, dataStart + 18 + nameLen).toString("utf8");
        layers.push({
          flags,
          layerType,
          childLevel,
          blendMode,
          opacity,
          name,
          visible: (flags & 1) === 1,
        });
      } else if (chunkType === CHUNK_PALETTE) {
        const numEntries = buffer.readUInt32LE(dataStart);
        const firstIndex = buffer.readUInt32LE(dataStart + 4);
        let pOff = dataStart + 20;
        for (let i = 0; i < numEntries && pOff + 6 <= dataEnd; i++) {
          const entryFlags = buffer.readUInt16LE(pOff);
          const r = buffer.readUInt8(pOff + 2);
          const g = buffer.readUInt8(pOff + 3);
          const b = buffer.readUInt8(pOff + 4);
          const a = buffer.readUInt8(pOff + 5);
          pOff += 6;
          if ((entryFlags & 1) !== 0) {
            const nLen = buffer.readUInt16LE(pOff);
            pOff += 2 + nLen;
          }
          const idx = firstIndex + i;
          if (idx >= 0 && idx < 256) {
            palette[idx] = [r, g, b, a];
          }
        }
      } else if (chunkType === CHUNK_OLD_PALETTE || chunkType === CHUNK_OLD_PALETTE2) {
        const packets = buffer.readUInt16LE(dataStart);
        let pOff = dataStart + 2;
        let colorIdx = 0;
        for (let p = 0; p < packets && pOff + 2 <= dataEnd; p++) {
          colorIdx += buffer.readUInt8(pOff);
          let count = buffer.readUInt8(pOff + 1);
          if (count === 0) count = 256;
          pOff += 2;
          for (let i = 0; i < count && pOff + 3 <= dataEnd; i++) {
            const r = buffer.readUInt8(pOff);
            const g = buffer.readUInt8(pOff + 1);
            const b = buffer.readUInt8(pOff + 2);
            pOff += 3;
            if (colorIdx < 256) {
              palette[colorIdx] = [r, g, b, 255];
            }
            colorIdx++;
          }
        }
      } else if (chunkType === CHUNK_FRAME_TAGS) {
        const tagCount = buffer.readUInt16LE(dataStart);
        let tOff = dataStart + 10;
        for (let t = 0; t < tagCount && tOff + 19 <= dataEnd; t++) {
          const from = buffer.readUInt16LE(tOff);
          const to = buffer.readUInt16LE(tOff + 2);
          const loopByte = buffer.readUInt8(tOff + 4);
          const nameLen = buffer.readUInt16LE(tOff + 17);
          const name = buffer.subarray(tOff + 19, tOff + 19 + nameLen).toString("utf8");
          tOff += 19 + nameLen;
          const loopDirection = loopByte === 1 ? "Reverse" : loopByte === 2 ? "PingPong" : "Forward";
          tags.push({ from, to, loopDirection, name });
        }
      } else if (chunkType === CHUNK_SLICE) {
        const numKeys = buffer.readUInt32LE(dataStart);
        const sFlags = buffer.readUInt32LE(dataStart + 4);
        const nameLen = buffer.readUInt16LE(dataStart + 12);
        const sName = buffer.subarray(dataStart + 14, dataStart + 14 + nameLen).toString("utf8");
        let kOff = dataStart + 14 + nameLen;
        const keys: InternalSliceKey[] = [];
        for (let k = 0; k < numKeys && kOff + 20 <= dataEnd; k++) {
          const frameNumber = buffer.readUInt32LE(kOff);
          const x = buffer.readInt32LE(kOff + 4);
          const y = buffer.readInt32LE(kOff + 8);
          const width = buffer.readUInt32LE(kOff + 12);
          const height = buffer.readUInt32LE(kOff + 16);
          kOff += 20;
          const keyObj: InternalSliceKey = { frameNumber, x, y, width, height };
          if ((sFlags & 1) !== 0 && kOff + 16 <= dataEnd) {
            keyObj.center = {
              x: buffer.readInt32LE(kOff),
              y: buffer.readInt32LE(kOff + 4),
              width: buffer.readUInt32LE(kOff + 8),
              height: buffer.readUInt32LE(kOff + 12),
            };
            kOff += 16;
          }
          if ((sFlags & 2) !== 0 && kOff + 8 <= dataEnd) {
            keyObj.pivot = {
              x: buffer.readInt32LE(kOff),
              y: buffer.readInt32LE(kOff + 4),
            };
            kOff += 8;
          }
          keys.push(keyObj);
        }
        slices.push({ name: sName, flags: sFlags, keys });
      } else if (chunkType === CHUNK_CEL) {
        const layerIndex = buffer.readUInt16LE(dataStart);
        const x = buffer.readInt16LE(dataStart + 2);
        const y = buffer.readInt16LE(dataStart + 4);
        const opacity = buffer.readUInt8(dataStart + 6);
        const celType = buffer.readUInt16LE(dataStart + 7);
        if (celType === 1) {
          const linkedFrame = buffer.readUInt16LE(dataStart + 16);
          celsThisFrame.push({ layerIndex, x, y, opacity, celType, linkedFrame, width: 0, height: 0 });
        } else if (celType === 0 || celType === 2) {
          const width = buffer.readUInt16LE(dataStart + 16);
          const height = buffer.readUInt16LE(dataStart + 18);
          const payload = buffer.subarray(dataStart + 20, dataEnd);
          const rawPixels = celType === 2 ? zlib.inflateSync(payload) : Buffer.from(payload);
          celsThisFrame.push({ layerIndex, x, y, opacity, celType, width, height, rawPixels });
        }
      }

      chunkOffset += chunkLength;
    }

    frameCels.push(celsThisFrame);
    offset += frameBytes;
  }

  const isLayerEffectiveVisible = (layerIdx: number): boolean => {
    const layer = layers[layerIdx];
    if (!layer || !layer.visible || layer.layerType === 1 || layer.name.startsWith("@")) {
      return false;
    }
    let currentLevel = layer.childLevel;
    for (let i = layerIdx - 1; i >= 0 && currentLevel > 0; i--) {
      if (layers[i].childLevel === currentLevel - 1) {
        if (!layers[i].visible) return false;
        currentLevel = layers[i].childLevel;
      }
    }
    return true;
  };

  const resolveCel = (frameIdx: number, cel: InternalCel): InternalCel => {
    if (cel.celType === 1 && cel.linkedFrame !== undefined && cel.linkedFrame < frameCels.length) {
      const target = frameCels[cel.linkedFrame].find((c) => c.layerIndex === cel.layerIndex);
      if (target) return resolveCel(cel.linkedFrame, target);
    }
    return cel;
  };

  const decodeCelToCanvasRgba = (cel: InternalCel, opacityMul: number): Buffer => {
    const out = Buffer.alloc(canvasWidth * canvasHeight * 4, 0);
    if (!cel.rawPixels || cel.width <= 0 || cel.height <= 0) return out;
    const bpp = colorDepth === 32 ? 4 : colorDepth === 16 ? 2 : 1;
    let srcIdx = 0;

    for (let cy = 0; cy < cel.height; cy++) {
      const dy = cel.y + cy;
      for (let cx = 0; cx < cel.width; cx++) {
        const dx = cel.x + cx;
        let r = 0,
          g = 0,
          b = 0,
          a = 0;
        if (colorDepth === 32 && srcIdx + 4 <= cel.rawPixels.length) {
          r = cel.rawPixels[srcIdx];
          g = cel.rawPixels[srcIdx + 1];
          b = cel.rawPixels[srcIdx + 2];
          a = cel.rawPixels[srcIdx + 3];
        } else if (colorDepth === 16 && srcIdx + 2 <= cel.rawPixels.length) {
          r = g = b = cel.rawPixels[srcIdx];
          a = cel.rawPixels[srcIdx + 1];
        } else if (colorDepth === 8 && srcIdx + 1 <= cel.rawPixels.length) {
          const idx = cel.rawPixels[srcIdx];
          if (idx !== transparentPaletteIndex && idx < 256) {
            [r, g, b, a] = palette[idx];
          }
        }
        srcIdx += bpp;

        if (dx >= 0 && dx < canvasWidth && dy >= 0 && dy < canvasHeight && a > 0) {
          const dstOff = (dy * canvasWidth + dx) * 4;
          out[dstOff] = r;
          out[dstOff + 1] = g;
          out[dstOff + 2] = b;
          out[dstOff + 3] = Math.round(a * opacityMul);
        }
      }
    }
    return out;
  };

  const blendNormalOver = (backdrop: Buffer, src: Buffer): void => {
    for (let i = 0; i < backdrop.length; i += 4) {
      const sa = src[i + 3] / 255;
      if (sa <= 0) continue;
      const ba = backdrop[i + 3] / 255;
      const outA = sa + ba * (1 - sa);
      if (outA <= 0) continue;
      backdrop[i] = Math.round((src[i] * sa + backdrop[i] * ba * (1 - sa)) / outA);
      backdrop[i + 1] = Math.round((src[i + 1] * sa + backdrop[i + 1] * ba * (1 - sa)) / outA);
      backdrop[i + 2] = Math.round((src[i + 2] * sa + backdrop[i + 2] * ba * (1 - sa)) / outA);
      backdrop[i + 3] = Math.round(outA * 255);
    }
  };

  const cropRgba = (src: Buffer, srcW: number, srcH: number, rx: number, ry: number, rw: number, rh: number): Buffer => {
    const out = Buffer.alloc(rw * rh * 4, 0);
    for (let y = 0; y < rh; y++) {
      const sy = ry + y;
      if (sy < 0 || sy >= srcH) continue;
      for (let x = 0; x < rw; x++) {
        const sx = rx + x;
        if (sx < 0 || sx >= srcW) continue;
        const sOff = (sy * srcW + sx) * 4;
        const dOff = (y * rw + x) * 4;
        out[dOff] = src[sOff];
        out[dOff + 1] = src[sOff + 1];
        out[dOff + 2] = src[sOff + 2];
        out[dOff + 3] = src[sOff + 3];
      }
    }
    return out;
  };

  const rawFrames: { rgba: Buffer; durationMs: number }[] = [];
  if (importType === "LayerToSprite") {
    for (let l = 0; l < layers.length; l++) {
      if (!isLayerEffectiveVisible(l)) continue;
      for (let f = 0; f < frameCount; f++) {
        const cels = frameCels[f].filter((c) => c.layerIndex === l);
        for (const rawCel of cels) {
          const cel = resolveCel(f, rawCel);
          const opacityMul = (layers[l].opacity / 255) * (cel.opacity / 255);
          rawFrames.push({
            rgba: decodeCelToCanvasRgba(cel, opacityMul),
            durationMs: frameDurations[f],
          });
        }
      }
    }
  } else {
    for (let f = 0; f < frameCount; f++) {
      const composite = Buffer.alloc(canvasWidth * canvasHeight * 4, 0);
      const sorted = [...frameCels[f]].sort((a, b) => a.layerIndex - b.layerIndex);
      for (const rawCel of sorted) {
        if (!isLayerEffectiveVisible(rawCel.layerIndex)) continue;
        const cel = resolveCel(f, rawCel);
        const layer = layers[rawCel.layerIndex];
        const opacityMul = (layer.opacity / 255) * (cel.opacity / 255);
        const celRgba = decodeCelToCanvasRgba(cel, opacityMul);
        blendNormalOver(composite, celRgba);
      }
      rawFrames.push({ rgba: composite, durationMs: frameDurations[f] });
    }
  }

  const sprites: AseImportedSprite[] = [];
  const addedNames = new Set<string>();
  const activeTags = importType === "Sprite" ? tags : [];
  const activeSlices = importType === "Sprite" ? slices : [];
  const sliceOnlySheet = activeSlices.length > 0 && rawFrames.length === 1 && activeTags.length === 0;

  const frameSprites: AseImportedSprite[] = [];
  for (let i = 0; i < rawFrames.length; i++) {
    let resolvedName = "";
    let matchedTag: string | undefined;
    for (const tag of activeTags) {
      if (tag.name.trim() && i >= tag.from && i <= tag.to) {
        matchedTag = tag.name.trim();
        resolvedName = tag.from === tag.to ? matchedTag : `${matchedTag}_${i - tag.from}`;
        break;
      }
    }
    if (!resolvedName || addedNames.has(resolvedName)) {
      resolvedName = `${fileName}_${i}`;
    }

    let spX = 0;
    let spY = 0;
    let spW = canvasWidth;
    let spH = canvasHeight;
    let spUnityRectY = 0;
    let spPivot = { ...defaultPivot };
    let spBorder = { left: 0, bottom: 0, right: 0, top: 0 };
    let spRgba = rawFrames[i].rgba;
    let matchedSliceName: string | undefined;

    for (const slice of activeSlices) {
      const sn = slice.name.trim();
      if (sn && slice.keys.length > 0 && (sn === resolvedName || (matchedTag && sn === matchedTag))) {
        const key = slice.keys[0];
        spX = Math.max(0, Math.min(canvasWidth, key.x));
        spY = Math.max(0, Math.min(canvasHeight, key.y));
        spW = Math.max(1, Math.min(canvasWidth - spX, key.width));
        spH = Math.max(1, Math.min(canvasHeight - spY, key.height));
        spUnityRectY = Math.max(0, Math.min(canvasHeight, canvasHeight - spY - spH));
        if (key.pivot && spW > 0 && spH > 0) {
          spPivot = { x: key.pivot.x / spW, y: 1 - key.pivot.y / spH };
        }
        if (key.center) {
          spBorder = {
            left: key.center.x,
            bottom: spH - key.center.y - key.center.height,
            right: spW - key.center.x - key.center.width,
            top: key.center.y,
          };
        }
        spRgba = cropRgba(rawFrames[i].rgba, canvasWidth, canvasHeight, spX, spY, spW, spH);
        matchedSliceName = sn;
        break;
      }
    }

    const sp: AseImportedSprite = {
      name: resolvedName,
      sourceFileName: fileName,
      frameIndex: i,
      width: spW,
      height: spH,
      x: spX,
      y: spY,
      unityRectY: spUnityRectY,
      pivot: spPivot,
      border: spBorder,
      pixelsPerUnit,
      durationMs: rawFrames[i].durationMs,
      tagName: matchedTag,
      sliceName: matchedSliceName,
      rgba: spRgba,
    };
    frameSprites.push(sp);
    if (!sliceOnlySheet) {
      addedNames.add(sp.name);
      sprites.push(sp);
    }
  }

  if (activeSlices.length > 0 && rawFrames.length > 0) {
    for (const slice of activeSlices) {
      if (!slice.name.trim() || slice.keys.length === 0) continue;
      for (let k = 0; k < slice.keys.length; k++) {
        const key = slice.keys[k];
        const frameIdx = Math.max(0, Math.min(rawFrames.length - 1, key.frameNumber));
        const rx = Math.max(0, Math.min(canvasWidth, key.x));
        const ry = Math.max(0, Math.min(canvasHeight, key.y));
        const rw = Math.max(1, Math.min(canvasWidth - rx, key.width));
        const rh = Math.max(1, Math.min(canvasHeight - ry, key.height));
        const unityRectY = Math.max(0, Math.min(canvasHeight, canvasHeight - ry - rh));

        const pivot =
          key.pivot && rw > 0 && rh > 0
            ? { x: key.pivot.x / rw, y: 1 - key.pivot.y / rh }
            : { ...defaultPivot };
        const border = key.center
          ? {
              left: key.center.x,
              bottom: rh - key.center.y - key.center.height,
              right: rw - key.center.x - key.center.width,
              top: key.center.y,
            }
          : { left: 0, bottom: 0, right: 0, top: 0 };

        const sliceSpriteName = slice.keys.length === 1 ? slice.name.trim() : `${slice.name.trim()}_${k}`;
        if (addedNames.has(sliceSpriteName)) continue;

        addedNames.add(sliceSpriteName);
        sprites.push({
          name: sliceSpriteName,
          sourceFileName: fileName,
          frameIndex: frameIdx,
          width: rw,
          height: rh,
          x: rx,
          y: ry,
          unityRectY,
          pivot,
          border,
          pixelsPerUnit,
          durationMs: rawFrames[frameIdx].durationMs,
          sliceName: slice.name.trim(),
          rgba: cropRgba(rawFrames[frameIdx].rgba, canvasWidth, canvasHeight, rx, ry, rw, rh),
        });
      }
    }
  }

  const animations: AseAnimationClipSpec[] = activeTags.map((tag) => {
    const length = tag.to - tag.from + 1;
    const indices: number[] = [];
    for (let i = 0; i < length; i++) {
      indices.push(tag.loopDirection === "Reverse" ? tag.to - i : tag.from + i);
    }
    return {
      name: `${fileName}_${tag.name}`,
      tagName: tag.name,
      frameFrom: tag.from,
      frameTo: tag.to,
      loopDirection: tag.loopDirection,
      loopTime: tag.name !== "Dead",
      spriteNames: indices.map((idx) => frameSprites[idx]?.name ?? `${fileName}_${idx}`),
      frameDurationsMs: indices.map((idx) => frameSprites[idx]?.durationMs ?? 100),
    };
  });

  return {
    fileName,
    width: canvasWidth,
    height: canvasHeight,
    frameCount,
    pixelsPerUnit,
    importType,
    layers: layers.map((l) => ({
      name: l.name,
      visible: l.visible,
      opacity: l.opacity,
      blendMode: l.blendMode,
      isGroup: l.layerType === 1,
      childLevel: l.childLevel,
    })),
    tags,
    slices,
    sprites,
    animations,
  };
}

export async function parseAsepriteFile(
  filePath: string,
  options?: {
    importType?: AseImportType;
    pixelsPerUnit?: number;
    defaultPivot?: { x: number; y: number };
  }
): Promise<AseImportResult> {
  const buf = await fs.promises.readFile(filePath);
  const ext = path.extname(filePath);
  const fileName = path.basename(filePath, ext);
  return parseAsepriteBuffer(buf, { ...options, fileName });
}

export async function aseSpriteToPngBuffer(sprite: AseImportedSprite): Promise<Buffer> {
  return await sharp(sprite.rgba, {
    raw: {
      width: sprite.width,
      height: sprite.height,
      channels: 4,
    },
  })
    .png()
    .toBuffer();
}
