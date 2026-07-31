import { saveAs } from 'file-saver';
import JSZip from 'jszip';

export function downloadBlob(blob, filename) {
  saveAs(blob, filename);
}

export async function downloadBatchAsZip(jobs) {
  const zip = new JSZip();
  jobs.forEach((job) => {
    if (job.result) {
      zip.file(job.file.name.replace(/\.pdf$/i, '.docx'), job.result);
    }
  });
  const content = await zip.generateAsync({ type: 'blob' });
  saveAs(content, 'converted-documents.zip');
}