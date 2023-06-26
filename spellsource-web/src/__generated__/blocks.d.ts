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
  messages?: Maybe<Array<Maybe<Scalars["String"]>>>;
  output?: Maybe<Scalars["String"]>;
  colour?: Maybe<Scalars["String"]>;
  nextStatement?: Maybe<Array<Maybe<Scalars["String"]>>>;
  previousStatement?: Maybe<Array<Maybe<Scalars["String"]>>>;
  args?: Maybe<Array<Maybe<BlockArgsDef>>>;
  data?: Maybe<Scalars["String"]>;
  inputsInline?: Maybe<Scalars["Boolean"]>;
  hat?: Maybe<Scalars["String"]>;
  comment?: Maybe<Scalars["String"]>;
  subcategory?: Maybe<Scalars["String"]>;
  plural?: Maybe<Scalars["Boolean"]>;
  id?: Maybe<Scalars["String"]>;
  path?: Maybe<Scalars["String"]>;
};

export type BlockArgsDef = {
  i: Scalars["Int"];
  args?: Maybe<Array<Maybe<BlockArgDef>>>;
};

export type BlockArgDef = {
  type?: Maybe<Scalars["String"]>;
  check?: Maybe<Array<Maybe<Scalars["String"]>> | Maybe<Scalars["string"]>>;
  name?: Maybe<Scalars["String"]>;
  valueI?: Maybe<Scalars["Int"]>;
  valueS?: Maybe<Scalars["String"]>;
  valueB?: Maybe<Scalars["Boolean"]>;
  min?: Maybe<Scalars["Int"]>;
  max?: Maybe<Scalars["Int"]>;
  int?: Maybe<Scalars["Boolean"]>;
  text?: Maybe<Scalars["String"]>;
  options?: Maybe<Array<Maybe<Array<Maybe<Scalars["String"]>>>>>;
  shadow?: Maybe<BlockShadowDef>;
  width?: Maybe<Scalars["Int"]>;
  height?: Maybe<Scalars["Int"]>;
  alt?: Maybe<Scalars["String"]>;
  src?: Maybe<Scalars["String"]>;
  variable?: Maybe<Scalars["String"]>;
  variableTypes?: Maybe<Array<Maybe<Scalars["String"]>>>;
  defaultType?: Maybe<Scalars["String"]>;
  value?: Maybe<Scalars["Int"] | Scalars["String"] | Scalars["Boolean"]>;
};

export type BlockShadowDef = {
  type?: Maybe<Scalars["String"]>;
  fields?: Maybe<Array<Maybe<BlockFieldDef>>>;
  notActuallyShadow?: Maybe<Scalars["Boolean"]>;
};

export type BlockFieldDef = {
  name?: Maybe<Scalars["String"]>;
  valueI?: Maybe<Scalars["Int"]>;
  valueS?: Maybe<Scalars["String"]>;
  valueB?: Maybe<Scalars["Boolean"]>;
};
