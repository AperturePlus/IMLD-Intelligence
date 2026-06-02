import { createApp } from "vue";
import App from "@/App.vue";
import { registerPlugins } from "@/app/providers/registerPlugins";
import "@/style.css";
import "@/styles/tokens.css";
//import '@/mock/httpMock'

const shellBridge = window.electron?.shell;
if (shellBridge?.frameless) {
  document.documentElement.classList.add("electron-shell");
  document.body.classList.add("electron-shell");
  document.documentElement.style.setProperty(
    "--electron-titlebar-safe-top",
    `${shellBridge.height}px`
  );
}

const app = createApp(App);

registerPlugins(app);
app.mount("#app");
