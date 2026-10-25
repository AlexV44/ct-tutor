import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // При сборке (npm run build) статика сразу падает в static папочку Spring Boot!
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
});