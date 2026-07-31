export async function generateDocxBlob(pdfData, settings, onProgress) {
  const worker = new Worker(new URL('../workers/docx.worker.js', import.meta.url), {
    type: 'module',
  });
  return new Promise((resolve, reject) => {
    worker.onmessage = (e) => {
      if (e.data.progress) onProgress?.(e.data.progress);
      else if (e.data.blob) {
        resolve(e.data.blob);
        worker.terminate();
      } else if (e.data.error) {
        reject(new Error(e.data.error));
        worker.terminate();
      }
    };
    worker.postMessage({ pdfData, settings });
  });
}