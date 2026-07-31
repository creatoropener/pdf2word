import { useSettings } from '../hooks/useSettings';
import { useState } from 'react';

export default function SettingsPanel({ onClose }) {
  const { settings, updateSettings } = useSettings();
  const [local, setLocal] = useState({ ...settings });

  const handleChange = (key, value) => {
    setLocal((prev) => ({ ...prev, [key]: value }));
  };

  const save = () => {
    updateSettings(local);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={onClose}>
      <div
        className="bg-white dark:bg-gray-900 rounded-2xl p-6 max-w-md w-full mx-4 shadow-xl max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="Settings"
      >
        <h2 className="text-lg font-bold mb-4">Settings</h2>
        <div className="space-y-4">
          <label className="flex items-center gap-2">
            <span className="text-sm">Max file size (MB)</span>
            <input
              type="number"
              value={local.maxFileSize / 1024 / 1024}
              onChange={(e) => handleChange('maxFileSize', +e.target.value * 1024 * 1024)}
              className="w-20 ml-auto border rounded px-2 py-1 dark:bg-gray-800"
            />
          </label>
          <label className="flex items-center gap-2">
            <span className="text-sm">Default OCR</span>
            <input
              type="checkbox"
              checked={local.ocr}
              onChange={(e) => handleChange('ocr', e.target.checked)}
              className="ml-auto"
            />
          </label>
          <label className="flex items-center gap-2">
            <span className="text-sm">Include images</span>
            <input
              type="checkbox"
              checked={local.includeImages}
              onChange={(e) => handleChange('includeImages', e.target.checked)}
              className="ml-auto"
            />
          </label>
          <label className="flex items-center gap-2">
            <span className="text-sm">Preserve formatting</span>
            <input
              type="checkbox"
              checked={local.preserveFormatting}
              onChange={(e) => handleChange('preserveFormatting', e.target.checked)}
              className="ml-auto"
            />
          </label>
          <label className="flex items-center gap-2">
            <span className="text-sm">Compress output</span>
            <input
              type="checkbox"
              checked={local.compressOutput}
              onChange={(e) => handleChange('compressOutput', e.target.checked)}
              className="ml-auto"
            />
          </label>
          <label className="flex flex-col gap-1">
            <span className="text-sm">Output filename (without extension)</span>
            <input
              type="text"
              value={local.outputFilename}
              onChange={(e) => handleChange('outputFilename', e.target.value)}
              className="border rounded px-2 py-1 dark:bg-gray-800"
            />
          </label>
        </div>
        <div className="flex gap-2 mt-6">
          <button
            onClick={onClose}
            className="flex-1 py-2 bg-gray-100 dark:bg-gray-800 rounded-lg"
          >
            Cancel
          </button>
          <button
            onClick={save}
            className="flex-1 py-2 bg-blue-600 text-white rounded-lg"
          >
            Save
          </button>
        </div>
      </div>
    </div>
  );
}