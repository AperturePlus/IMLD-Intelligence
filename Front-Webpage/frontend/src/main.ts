import { createApp } from 'vue'
import App from '@/App.vue'
import { registerPlugins } from '@/app/providers/registerPlugins'
import '@/style.css'

const app = createApp(App)

registerPlugins(app)
app.mount('#app')
