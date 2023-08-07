import { ConnectionState } from "blockly/core/serialization/blocks";

export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = {
  [K in keyof T]: T[K];
};
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};

export type BlockDef = {
  type?: Maybe<Scalars["String"]>;
  output?: Maybe<Scalars["String"]>;
  colour?: Maybe<Scalars["String"]>;
  nextStatement?: Maybe<Array<Maybe<Scalars["String"]>>>;
  previousStatement?: Maybe<Array<Maybe<Scalars["String"]>>>;
  data?: Maybe<Scalars["String"]>;
  inputsInline?: Maybe<Scalars["Boolean"]>;
  hat?: Maybe<Scalars["String"]>;
  comment?: Maybe<Scalars["String"]>;
  subcategory?: Maybe<Scalars["String"]>;
  plural?: Maybe<Scalars["Boolean"]>;
  id?: Maybe<Scalars["String"]>;
  path?: Maybe<Scalars["String"]>;
  value?: Maybe<Scalars["Int"]>;
  mutator?: Maybe<Scalars["String"]>;
  enableContextMenu?: Maybe<Scalars["Boolean"]>;
  mutatorOptions?: any;
  next?: ConnectionState;
} & {
  [K in `message${number}`]?: Maybe<Scalars["String"]>;
} & {
  [K in `args${number}`]?: Maybe<Array<Maybe<BlockArgDef>>>;
};

export type BlockArgDef = {
  type?: Maybe<Scalars["String"]>;
  check?: Maybe<Array<Maybe<Scalars["String"]>> | Maybe<Scalars["string"]>>;
  name?: Maybe<Scalars["String"]>;
  min?: Maybe<Scalars["Int"]>;
  max?: Maybe<Scalars["Int"]>;
  int?: Maybe<Scalars["Boolean"]>;
  text?: Maybe<Scalars["String"]>;
  options?: Maybe<Array<Maybe<Array<Maybe<Scalars["String"]>>>>>;
  width?: Maybe<Scalars["Int"]>;
  height?: Maybe<Scalars["Int"]>;
  alt?: Maybe<Scalars["String"]>;
  src?: Maybe<Scalars["String"]>;
  variable?: Maybe<Scalars["String"]>;
  variableTypes?: Maybe<Array<Maybe<Scalars["String"]>>>;
  defaultType?: Maybe<Scalars["String"]>;
  value?: Maybe<Scalars["Int"] | Scalars["String"] | Scalars["Boolean"]>;
  next?: Maybe<BlockShadowDef>;
  optional?: Maybe<Scalars["Boolean"]>;
  checked?: Maybe<Scalars["Boolean"]>;
} & ConnectionState;
