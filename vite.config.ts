import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { resolve } from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        wallet: resolve(__dirname, 'wallet.html'),
        tablet: resolve(__dirname, 'tablet.html'),
        studio: resolve(__dirname, 'studio.html')
      }
    }
  },
  server: {
    port: 5174,
    host: true
  }
});
