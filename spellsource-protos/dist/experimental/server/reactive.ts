/* eslint-disable */
import _m0 from "protobufjs/minimal";

export const protobufPackage = "spellsource";

export interface NotificationKindMessage {}

export enum NotificationKindMessage_NotificationKind {
  ADDED = 0,
  CHANGED = 1,
  REMOVED = 2,
  UNRECOGNIZED = -1,
}

export interface AddedChangedRemoved {
  kind: NotificationKindMessage_NotificationKind;
  fields: number[];
}

function createBaseNotificationKindMessage(): NotificationKindMessage {
  return {};
}

export const NotificationKindMessage = {
  encode(_: NotificationKindMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): NotificationKindMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseNotificationKindMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<NotificationKindMessage>, I>>(base?: I): NotificationKindMessage {
    return NotificationKindMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<NotificationKindMessage>, I>>(_: I): NotificationKindMessage {
    const message = createBaseNotificationKindMessage();
    return message;
  },
};

function createBaseAddedChangedRemoved(): AddedChangedRemoved {
  return { kind: 0, fields: [] };
}

export const AddedChangedRemoved = {
  encode(message: AddedChangedRemoved, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.kind !== 0) {
      writer.uint32(8).int32(message.kind);
    }
    writer.uint32(18).fork();
    for (const v of message.fields) {
      writer.int32(v);
    }
    writer.ldelim();
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AddedChangedRemoved {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAddedChangedRemoved();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.kind = reader.int32() as any;
          continue;
        case 2:
          if (tag === 16) {
            message.fields.push(reader.int32());

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.fields.push(reader.int32());
            }

            continue;
          }

          break;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<AddedChangedRemoved>, I>>(base?: I): AddedChangedRemoved {
    return AddedChangedRemoved.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AddedChangedRemoved>, I>>(object: I): AddedChangedRemoved {
    const message = createBaseAddedChangedRemoved();
    message.kind = object.kind ?? 0;
    message.fields = object.fields?.map((e) => e) || [];
    return message;
  },
};

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T : T extends globalThis.Array<infer U> ? globalThis.Array<DeepPartial<U>> : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>> : T extends { $case: string } ? { [K in keyof Omit<T, "$case">]?: DeepPartial<T[K]> } & { $case: T["$case"] } : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> } : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };
