import { downloadBlob } from './downloadUtils';

export async function downloadAsTxt(pdfData, filenameBase) {
  const text = pdfData.pages.map(p => p.paragraphs.map(p => p.text).join('\n')).join('\n\n');
  const blob = new Blob([text], { type: 'text/plain' });
  downloadBlob(blob, `${filenameBase}.txt`);
}

export async function downloadAsMarkdown(pdfData, filenameBase) {
  let md = '';
  pdfData.pages.forEach((page) => {
    page.paragraphs.forEach(p => {
      if (p.type === 'heading') md += `## ${p.text}\n\n`;
      else if (p.type === 'listItem') md += `- ${p.text}\n`;
      else md += `${p.text}\n\n`;
    });
  });
  const blob = new Blob([md], { type: 'text/markdown' });
  downloadBlob(blob, `${filenameBase}.md`);
}