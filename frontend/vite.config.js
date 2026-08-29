import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173
  },
  build: {
    rollupOptions: {
      output: {
        // React y el SDK de Auth0 cambian mucho menos que el codigo propio;
        // separarlos permite al navegador reutilizarlos entre despliegues.
        manualChunks: {
          react: ['react', 'react-dom'],
          auth: ['@auth0/auth0-react']
        }
      }
    }
  }
});
