import Tesseract from 'tesseract.js';

export async function runOCR(pdfData, onProgress) {
  const total = pdfData.pages.length;
  for (let i = 0; i < total; i++) {
    const page = pdfData.pages[i];
    const textLength = page.paragraphs.reduce((sum, p) => sum + p.text.length, 0);
    if (textLength < 20) {
      const result = await Tesseract.recognize(page.imageBlob, 'eng', {
        logger: (m) => onProgress?.((i + m.progress) / total),
      });
      page.paragraphs = [
        { type: 'paragraph', text: result.data.text, bold: false, fontSize: 12 },
      ];
    }
    onProgress?.((i + 1) / total);
  }
}