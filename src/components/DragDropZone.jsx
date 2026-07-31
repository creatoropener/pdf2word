import { useCallback, useRef, useState } from 'react';
import { useFileUpload } from '../hooks/useFileUpload';
import { useToast } from '../hooks/useToast';

export default function DragDropZone({ onFilesAdded }) {
  const [isDragOver, setIsDragOver] = useState(false);
  const inputRef = useRef(null);
  const { addFiles } = useFileUpload();
  const { addToast } = useToast();

  const handleDrop = useCallback(
    (e) => {
      e.preventDefault();
      setIsDragOver(false);
      const files = [...e.dataTransfer.files].filter(
        (f) => f.type === 'application/pdf'
      );
      if (files.length === 0) {
        addToast('Please drop PDF files only.', 'warning');
        return;
      }
      const errs = addFiles(files);
      if (errs.length > 0) errs.forEach((e) => addToast(e.error, 'error'));
      else onFilesAdded?.(files);
    },
    [addFiles, addToast, onFilesAdded]
  );

  const handleClick = () => inputRef.current?.click();
  const handleFileChange = (e) => {
    const files = [...e.target.files];
    if (files.length) {
      const errs = addFiles(files);
      errs.forEach((e) => addToast(e.error, 'error'));
      onFilesAdded?.(files);
    }
    e.target.value = '';
  };

  return (
    <div
      className={`relative border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all duration-300 ${
        isDragOver ? 'drag-over' : 'border-gray-300 dark:border-gray-700'
      }`}
      onDragOver={(e) => { e.preventDefault(); setIsDragOver(true); }}
      onDragLeave={() => setIsDragOver(false)}
      onDrop={handleDrop}
      onClick={handleClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && handleClick()}
      aria-label="Upload PDF files"
    >
      <input
        ref={inputRef}
        type="file"
        accept="application/pdf"
        multiple
        className="hidden"
        onChange={handleFileChange}
      />
      <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
      </svg>
      <p className="mt-2 text-sm font-medium">
        Drag & drop PDF files here, or click to browse
      </p>
      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
        Max file size: 100 MB
      </p>
    </div>
  );
}