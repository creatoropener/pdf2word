import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  optimizeDeps: {
    include: ['pdfjs-dist', 'docx', 'tesseract.js', 'jszip', 'file-saver', 'idb'],
  },
  worker: {
    format: 'es',
  },
});