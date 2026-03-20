import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

const app=createApp(App)

// 引入路由，并注册
import router from './router/index'
app.use(router)


// 引入element-plus,并注册全部的element官方图标
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 引入TDesignChat,并注册，便于快速构建聊天界面
import TDesignChat from '@tdesign-vue-next/chat'; // 引入chat组件
app.use(TDesignChat);

// 引入VueMarkdownEditor,并注册，便于快速构建markdown编辑器，(v-md-editor 组件)
import VueMarkdownEditor from '@kangc/v-md-editor';
import '@kangc/v-md-editor/lib/style/base-editor.css';

// 主题，这里使用vuepress主题
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js';
import '@kangc/v-md-editor/lib/theme/style/vuepress.css';

// 网页代码高亮，这里使用prism，实现网页中代码高亮
import Prism from 'prismjs';
VueMarkdownEditor.use(vuepressTheme, {
  Prism,
});

// 引入 CodeMirror 的各种语言模式支持
import 'codemirror/mode/markdown/markdown';      // 支持 Markdown 语法高亮
import 'codemirror/mode/javascript/javascript';  // 支持 JavaScript 语法高亮
import 'codemirror/mode/css/css';                // 支持 CSS 语法高亮
import 'codemirror/mode/htmlmixed/htmlmixed';    // 支持 HTML（混合模式）语法高亮
import 'codemirror/mode/vue/vue';                // 支持 Vue 文件语法高亮

// 引入 CodeMirror 编辑相关插件
import 'codemirror/addon/edit/closebrackets';    // 自动补全括号
import 'codemirror/addon/edit/closetag';         // 自动补全标签
import 'codemirror/addon/edit/matchbrackets';    // 高亮匹配的括号

// 引入 CodeMirror 占位符插件
import 'codemirror/addon/display/placeholder';   // 支持编辑器占位符显示

// 引入 CodeMirror 选中行高亮插件
import 'codemirror/addon/selection/active-line'; // 高亮当前活动行

// 引入 CodeMirror 滚动条插件及样式
import 'codemirror/addon/scroll/simplescrollbars';     // 简易自定义滚动条
import 'codemirror/addon/scroll/simplescrollbars.css'; // 简易滚动条样式

// 引入 CodeMirror 编辑器基础样式
import 'codemirror/lib/codemirror.css';         // 编辑器基础样式
app.use(VueMarkdownEditor);
app.mount('#app')

// VueMarkdownEditor（@kangc/v-md-editor）是一个基于 Vue 的 Markdown 编辑器组件，
// 负责实现 Markdown 的编辑和预览功能。
// CodeMirror 是一个强大的代码编辑器库，支持多种语言高亮、补全等功能，
// 在 VueMarkdownEditor 中作为底层代码编辑核心，
// 负责代码输入区的高亮、补全、滚动条等编辑体验。
// 两者配合，让网页拥有强大的 Markdown 编辑和代码
