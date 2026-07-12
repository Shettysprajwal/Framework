import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api/v1/ledger': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/monitor': {
        target: 'http://localhost:8089',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/governance': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/zkp': {
        target: 'http://localhost:8087',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/pqc': {
        target: 'http://localhost:8086',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/pdp': {
        target: 'http://localhost:8085',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/pip': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/policies': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/translation': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      },
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
