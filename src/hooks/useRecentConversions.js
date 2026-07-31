import { Document, Packer, Paragraph, TextRun, ImageRun, PageBreak } from 'docx';

self.onmessage = async (e) => {
  try {
    const { pdfData, settings } = e.data;
    const { pages } = pdfData;
    const children = [];

    for (let i = 0; i < pages.length; i++) {
      const page = pages[i];
      if (i > 0) children.push(new Paragraph({ children: [new PageBreak()] }));

      if (settings.includeImages !== false) {
        const imgBuffer = await page.imageBlob.arrayBuffer();
        children.push(
          new Paragraph({
            children: [
              new ImageRun({
                data: imgBuffer,
                transformation: { width: page.width / 1.5, height: page.height / 1.5 },
              }),
            ],
          })
        );
      }

      page.paragraphs.forEach((p) => {
        children.push(
          new Paragraph({
            children: [
              new TextRun({
                text: p.text,
                bold: p.bold,
                size: Math.round(p.fontSize * 2) || 24,
              }),
            ],
            heading: p.type === 'heading' ? 'Heading2' : undefined,
            bullet: p.type === 'listItem' ? { level: 0 } : undefined,
          })
        );
      });
    }

    const doc = new Document({ sections: [{ children }] });
    const blob = await Packer.toBlob(doc);
    self.postMessage({ blob });
  } catch (error) {
    self.postMessage({ error: error.message });
  }
};