import { useState, useEffect, useCallback } from 'react';
import DragDropZone from '../components/DragDropZone';
import FileList from '../components/FileList';
import ConversionOptions from '../components/ConversionOptions';
import ProgressSection from '../components/ProgressSection';
import PreviewPanel from '../components/PreviewPanel';
import { useFileUpload } from '../hooks/useFileUpload';
import { useConversion } from '../hooks/useConversion';
import { downloadBlob, downloadBatchAsZip } from '../utils/downloadUtils';
import Hero from '../components/Hero';
import FeatureCards from '../components/FeatureCards';
import FAQ from '../components/FAQ';
import PrivacySection from '../components/PrivacySection';
import RecentConversions from '../components/RecentConversions';

export default function HomePage() {
  const { files, errors, addFiles, removeFile, clearFiles } = useFileUpload();
  const { jobs, startConversion } = useConversion(files);
  const [selectedFile, setSelectedFile] = useState(null);

  const handleFilesAdded = (newFiles) => {
    if (newFiles.length) setSelectedFile(newFiles[0]);
  };

  // Keyboard shortcut listener for Ctrl+Shift+C
  useEffect(() => {
    const handler = () => startConversion();
    window.addEventListener('start-conversion', handler);
    return () => window.removeEventListener('start-conversion', handler);
  }, [startConversion]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-12 space-y-16">
      <Hero />
      <DragDropZone onFilesAdded={handleFilesAdded} />
      {files.length > 0 && (
        <section className="space-y-6 animate-fade-in">
          <FileList files={files} onRemove={removeFile} onClear={clearFiles} />
          <ConversionOptions />
          <button
            onClick={startConversion}
            className="w-full py-3 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 transition"
          >
            Convert {files.length} file{files.length > 1 ? 's' : ''}
          </button>
          <ProgressSection jobs={jobs} />
        </section>
      )}
      {jobs.some((j) => j.status === 'done') && (
        <div className="flex gap-4 flex-wrap">
          {jobs
            .filter((j) => j.status === 'done')
            .map((job) => (
              <button
                key={job.id}
                onClick={() => downloadBlob(job.result, job.file.name.replace(/\.pdf$/i, '.docx'))}
                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
              >
                Download {job.file.name}
              </button>
            ))}
          {jobs.filter((j) => j.status === 'done').length > 1 && (
            <button
              onClick={() => downloadBatchAsZip(jobs.filter((j) => j.result))}
              className="px-4 py-2 bg-gray-600 text-white rounded-lg"
            >
              Download all as ZIP
            </button>
          )}
        </div>
      )}
      {selectedFile && (
        <PreviewPanel file={selectedFile} pages={[1, 5]} />
      )}
      <FeatureCards />
      <FAQ />
      <PrivacySection />
      <RecentConversions jobs={jobs} />
    </div>
  );
}