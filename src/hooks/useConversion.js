import { useState, useCallback, useRef } from 'react';
import { parsePdf } from '../utils/pdfParser';
import { generateDocxBlob } from '../utils/docxGenerator';
import { useSettings } from './useSettings';
import { useToast } from './useToast';
import { runOCR } from '../utils/ocrUtils';

export function useConversion(files) {
  const [jobs, setJobs] = useState([]);
  const { settings } = useSettings();
  const { addToast } = useToast();
  const abortRef = useRef(false);

  const startConversion = useCallback(async () => {
    abortRef.current = false;
    const newJobs = files.map((file) => ({
      id: crypto.randomUUID(),
      file,
      status: 'pending',
      progress: 0,
      result: null,
    }));
    setJobs(newJobs);
    for (const job of newJobs) {
      if (abortRef.current) break;
      try {
        updateJob(job.id, { status: 'parsing', progress: 0 });
        const pdfData = await parsePdf(job.file, settings.pageRange, (p) =>
          updateJob(job.id, { progress: p * 0.4 })
        );
        if (settings.ocr) {
          updateJob(job.id, { status: 'ocr', progress: 0.4 });
          await runOCR(pdfData, (p) =>
            updateJob(job.id, { progress: 0.4 + p * 0.4 })
          );
        }
        updateJob(job.id, { status: 'generating', progress: 0.8 });
        const blob = await generateDocxBlob(pdfData, settings, (p) =>
          updateJob(job.id, { progress: 0.8 + p * 0.2 })
        );
        updateJob(job.id, { status: 'done', progress: 1, result: blob, pages: pdfData.totalPages });
      } catch (err) {
        updateJob(job.id, { status: 'error', error: err.message });
        addToast(`Error converting ${job.file.name}: ${err.message}`, 'error');
      }
    }
  }, [files, settings, addToast]);

  const updateJob = (id, updates) => {
    setJobs((prev) =>
      prev.map((j) => (j.id === id ? { ...j, ...updates } : j))
    );
  };

  const cancel = () => {
    abortRef.current = true;
  };

  return { jobs, startConversion, cancel };
}