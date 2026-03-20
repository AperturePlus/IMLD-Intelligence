import Vue from 'vue'
import App from './App'
import store from './store' // store
import plugins from './plugins' // plugins
import './permission' // permission
/*import Vue from 'vue'：
导入 Vue 库，用于创建 Vue 实例。
import App from './App'：
导入根组件 App.vue，作为应用的入口组件。
import store from './store'：
导入 Vuex 状态管理实例，用于全局状态管理。
import plugins from './plugins'：
导入自定义插件，用于扩展 Vue 的功能。
import './permission'：
导入权限控制模块，用于处理路由权限或全局权限逻辑*/

Vue.use(plugins)//注册自定义插件，扩展 Vue 的功能。
Vue.config.productionTip = false
Vue.prototype.$store = store

App.mpType = 'app'

const app = new Vue({
  ...App
})

app.$mount()
