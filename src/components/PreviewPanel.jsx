import { useState, useEffect } from 'react';
import { Document, Page, pdfjs } from 'pdfjs-dist';

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url
).toString();

export default function PreviewPanel({ file, pages }) {
  const [thumbnails, setThumbnails] = useState([]);
  const [textPreview, setTextPreview] = useState('');

  useEffect(() => {
    if (!file) return;
    let cancelled = false;
    const loadPreview = async () => {
      const arrayBuf = await file.arrayBuffer();
      const pdf = await pdfjs.getDocument({ data: arrayBuf }).promise;
      const total = pdf.numPages;
      const thumbUrls = [];
      let text = '';
      const range = pages || [1, Math.min(total, 10)];
      for (let i = range[0]; i <= range[1]; i++) {
        if (cancelled) break;
        const page = await pdf.getPage(i);
        const viewport = page.getViewport({ scale: 0.3 });
        const canvas = document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        const ctx = canvas.getContext('2d');
        await page.render({ canvasContext: ctx, viewport }).promise;
        thumbUrls.push(canvas.toDataURL());
        const textContent = await page.getTextContent();
        text += textContent.items.map((it) => it.str).join(' ') + '\n';
      }
      if (!cancelled) {
        setThumbnails(thumbUrls);
        setTextPreview(text.slice(0, 2000));
      }
    };
    loadPreview().catch(console.error);
    return () => { cancelled = true; };
  }, [file, pages]);

  return (
    <div className="p-4 bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-800">
      <h3 className="text-lg font-semibold mb-4">Preview</h3>
      <div className="flex gap-2 overflow-x-auto pb-2">
        {thumbnails.map((src, i) => (
          <img
            key={i}
            src={src}
            alt={`Page ${i + 1}`}
            className="h-24 rounded shadow"
          />
        ))}
      </div>
      <pre className="mt-4 text-xs bg-gray-50 dark:bg-gray-800 p-3 rounded max-h-40 overflow-auto">
        {textPreview || 'No text extracted yet.'}
      </pre>
    </div>
  );
}