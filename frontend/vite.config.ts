import { reactRouter } from "@react-router/dev/vite"
import tailwindcss from "@tailwindcss/vite"
import { defineConfig } from "vite"

export default defineConfig({
  resolve: { tsconfigPaths: true },
  plugins: [tailwindcss(), reactRouter()],
  server: {
    host: "0.0.0.0",
    port: 5173,
    allowedHosts: ["racoon-turtle-avenging.ngrok-free.dev"],
    proxy: {
      '/api': {
        target: 'http://backend-dev:8080',
        changeOrigin: true,
        secure: false,
      },
    }
  },
})
