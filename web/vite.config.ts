import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
  // Electron packaged app is loaded through file://, so assets must use relative paths.
  base: mode === "electron" ? "./" : "/",
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      "@": "/src",
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
      },
    },
  },
}));
