const zlib = require('zlib');

const Aseprite = require('./aseprite');
const KaitaiStream = require('kaitai-struct/KaitaiStream');

const ChunkTypeEnum = Aseprite.Frame.Chunk.ChunkTypeEnum;
const CelTypeEnum = Aseprite.Frame.Chunk.CelChunk.CelTypeEnum;

const cleanProps = [
	'_io',
	'_parent',
	'_root',
	'_dataView',
	'_byteLength'
];

function clean(object) {
	if (typeof object === 'object' && object !== null) {
		if (Array.isArray(object)) {
			for (const v of object) {
				clean(v);
			}
		} else {
			for (const prop of cleanProps) {
				delete object[prop];
			}

			for (const key of Object.keys(object)) {
				clean(object[key]);
			}
		}
	}

	return object;
}

function inflate(ase) {
	// Iterate all frames, searching for cel chunks with compressed pixel data.
	for (const frame of ase.frames) {
		for (const chunk of frame.chunks) {
			if (chunk.type === ChunkTypeEnum.CEL) {
				const cel = chunk.data;
				if (cel.type === CelTypeEnum.COMPRESSED) {
					cel.pixels = zlib.inflateSync(cel.pixelsCompressed);
				}
			}
		}
	}

	return ase;
}

function parse(content, options) {
	options = options || {};
	options.clean = options.clean === undefined || options.clean;
	options.inflate = options.inflate === undefined || options.inflate;

	let ase = new Aseprite(new KaitaiStream(content));

	if (options.clean) {
		ase = clean(ase);
	}

	if (options.inflate) {
		ase = inflate(ase);
	}

	return ase;
}

exports.parse = parse;
exports.clean = clean;
exports.inflate = inflate;
exports.Frame = Aseprite.Frame;
exports.Header = Aseprite.Header;

exports.default = exports;
