// Stop imports yelling
declare module "*.png" {
  const value: any;
  export = value;
}
declare module '*.scss' {
  const content: Record<string, string>;
  export default content;
}
declare module '!!raw-loader!*.css' {
  const value: string;
  export = value;
}