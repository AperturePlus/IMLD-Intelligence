import Vue from 'vue'
import App from './App.vue'
import store from './store'
import plugins from './plugins'
import './permission'

Vue.use(plugins)
Vue.config.productionTip = false
Vue.prototype.$store = store

;(App as any).mpType = 'app'

const app = new Vue({
  ...(App as any)
})

app.$mount()
