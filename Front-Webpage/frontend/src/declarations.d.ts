//这个是声明非官方的TS库来源，先声明了才能开始引用
declare module 'prismjs' {
  const Prism: any;
  export default Prism;
}

declare module '@kangc/v-md-editor' {
  const VueMarkdownEditor: any;
  export default VueMarkdownEditor;
}

declare module '@kangc/v-md-editor/lib/theme/vuepress.js' {
  const vuepressTheme: any;
  export default vuepressTheme;
}