# PDF to DOCX Converter (100% Client-Side)

Convert PDF files to editable Word documents directly in your browser.  
No upload, no server, complete privacy.

## Features
- Drag & drop or browse PDF files
- Preserves text, formatting, images, lists, and headings
- Optional OCR for scanned pages (Tesseract.js)
- Batch conversion with ZIP download
- Dark/light mode, responsive design
- Fully offline capable (PWA)

## Quick Start
```bash
npm install
npm run dev
```

## Build for Production
```bash
npm run build
npm run preview
```

## Deploy to Vercel / Netlify
Just connect your Git repository and set the build command to `npm run build` and the output directory to `dist`.

**Vercel**: No extra configuration needed.  
**Netlify**: Set publish directory to `dist`, build command `npm run build`.

## Privacy
All processing happens locally in your browser. Your files never leave your device.

## Tech Stack
React + Vite, Tailwind CSS, pdf.js, docx, Tesseract.js, Web Workers, FileSaver.js