import tab from './tab'
import auth from './auth'
import modal from './modal'

const plugins = {
  install(Vue: any): void {
    Vue.prototype.$tab = tab
    Vue.prototype.$auth = auth
    Vue.prototype.$modal = modal
  }
}

export default plugins
