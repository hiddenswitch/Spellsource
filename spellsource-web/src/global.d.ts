// Stop imports yelling
declare module "*.png" {
  const value: any;
  export = value;
}
declare module "*.gif" {
  const value: any;
  export = value;
}
declare module '!!raw-loader!*.css' {
  const value: string;
  export = value;
}

declare global {
  interface Element {
    style: CSSStyleDeclaration
    innerText: string
  }
}