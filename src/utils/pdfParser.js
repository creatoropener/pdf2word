import * as pdfjsLib from 'pdfjs-dist';

export async function parsePdf(file, pageRange = [1, Infinity], onProgress) {
  const arrayBuf = await file.arrayBuffer();
  const pdf = await pdfjsLib.getDocument({ data: arrayBuf }).promise;
  const totalPages = pdf.numPages;
  const start = Math.max(1, pageRange[0]);
  const end = Math.min(totalPages, pageRange[1] || totalPages);
  const pages = [];

  for (let i = start; i <= end; i++) {
    const page = await pdf.getPage(i);
    const viewport = page.getViewport({ scale: 1.5 });
    const canvas = document.createElement('canvas');
    canvas.width = viewport.width;
    canvas.height = viewport.height;
    const ctx = canvas.getContext('2d');
    await page.render({ canvasContext: ctx, viewport }).promise;
    const imageBlob = await new Promise((res) => canvas.toBlob(res, 'image/png'));

    const textContent = await page.getTextContent();
    const paragraphs = groupTextToParagraphs(textContent.items, viewport.height);

    pages.push({ imageBlob, paragraphs, width: viewport.width, height: viewport.height });
    if (onProgress) onProgress((i - start + 1) / (end - start + 1));
  }
  return { pages, totalPages };
}

function groupTextToParagraphs(items, pageHeight) {
  const linesMap = new Map();
  items.forEach((item) => {
    const y = Math.round(item.transform[5]);
    if (!linesMap.has(y)) linesMap.set(y, []);
    linesMap.get(y).push(item);
  });
  const lines = [...linesMap.entries()]
    .sort((a, b) => a[0] - b[0])
    .map(([y, items]) => ({
      y,
      text: items.map((it) => it.str).join(' '),
      fontSize: Math.max(...items.map((it) => it.height || 12)),
      bold: items.some((it) => it.fontName?.toLowerCase().includes('bold')),
    }));

  const paragraphs = [];
  let currentPara = null;
  lines.forEach((line) => {
    const isHeading = line.fontSize > 16 && line.bold;
    const isListItem = /^[\s]*[•\-\d+\.]\s/.test(line.text);
    if (isHeading || isListItem || !currentPara) {
      if (currentPara) paragraphs.push(currentPara);
      currentPara = {
        type: isHeading ? 'heading' : isListItem ? 'listItem' : 'paragraph',
        text: line.text,
        fontSize: line.fontSize,
        bold: line.bold,
      };
    } else {
      currentPara.text += ' ' + line.text;
    }
  });
  if (currentPara) paragraphs.push(currentPara);
  return paragraphs;
}