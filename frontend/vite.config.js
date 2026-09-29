import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Proxy /api to the Spring Boot backend so the dev server needs no CORS setup.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
