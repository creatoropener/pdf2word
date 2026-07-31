import { useSettings } from '../hooks/useSettings';
import { useState } from 'react';

export default function ConversionOptions() {
  const { settings, updateSettings } = useSettings();
  const [local, setLocal] = useState({
    pageRange: settings.pageRange?.join('-') || '',
    ocr: settings.ocr,
    includeImages: settings.includeImages,
    preserveFormatting: settings.preserveFormatting,
    compressOutput: settings.compressOutput,
    outputFilename: settings.outputFilename,
  });

  const apply = () => {
    const [start, end = Infinity] = local.pageRange
      .split('-')
      .map((s) => +s)
      .filter((n) => !isNaN(n));
    updateSettings({
      pageRange: start ? [start, end === Infinity ? undefined : end] : [1, undefined],
      ocr: local.ocr,
      includeImages: local.includeImages,
      preserveFormatting: local.preserveFormatting,
      compressOutput: local.compressOutput,
      outputFilename: local.outputFilename,
    });
  };

  return (
    <details className="group border border-gray-200 dark:border-gray-700 rounded-xl p-4">
      <summary className="font-medium cursor-pointer">Conversion Options</summary>
      <div className="mt-4 space-y-4">
        <label className="flex items-center gap-2">
          <span className="text-sm">Page range (e.g. 1-10)</span>
          <input
            type="text"
            value={local.pageRange}
            onChange={(e) => setLocal((p) => ({ ...p, pageRange: e.target.value }))}
            className="ml-auto border rounded px-2 py-1 w-24 dark:bg-gray-800"
          />
        </label>
        <label className="flex items-center gap-2">
          <span className="text-sm">Enable OCR</span>
          <input
            type="checkbox"
            checked={local.ocr}
            onChange={(e) => setLocal((p) => ({ ...p, ocr: e.target.checked }))}
            className="ml-auto"
          />
        </label>
        <label className="flex items-center gap-2">
          <span className="text-sm">Include images</span>
          <input
            type="checkbox"
            checked={local.includeImages}
            onChange={(e) => setLocal((p) => ({ ...p, includeImages: e.target.checked }))}
            className="ml-auto"
          />
        </label>
        <label className="flex items-center gap-2">
          <span className="text-sm">Preserve formatting</span>
          <input
            type="checkbox"
            checked={local.preserveFormatting}
            onChange={(e) => setLocal((p) => ({ ...p, preserveFormatting: e.target.checked }))}
            className="ml-auto"
          />
        </label>
        <label className="flex items-center gap-2">
          <span className="text-sm">Compress output</span>
          <input
            type="checkbox"
            checked={local.compressOutput}
            onChange={(e) => setLocal((p) => ({ ...p, compressOutput: e.target.checked }))}
            className="ml-auto"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm">Output filename</span>
          <input
            type="text"
            value={local.outputFilename}
            onChange={(e) => setLocal((p) => ({ ...p, outputFilename: e.target.value }))}
            className="border rounded px-2 py-1 dark:bg-gray-800"
          />
        </label>
        <button
          onClick={apply}
          className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-lg"
        >
          Apply
        </button>
      </div>
    </details>
  );
}