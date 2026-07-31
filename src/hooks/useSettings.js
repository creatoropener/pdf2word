import { createContext, useContext, useState } from 'react';

const SettingsContext = createContext();

const defaultSettings = {
  maxFileSize: 100 * 1024 * 1024,
  pageRange: [1, Infinity],
  ocr: false,
  includeImages: true,
  preserveFormatting: true,
  compressOutput: false,
  outputFilename: 'converted',
};

export function SettingsProvider({ children }) {
  const [settings, setSettings] = useState(() => {
    const saved = localStorage.getItem('app-settings');
    return saved ? { ...defaultSettings, ...JSON.parse(saved) } : defaultSettings;
  });
  const updateSettings = (newSettings) => {
    const merged = { ...settings, ...newSettings };
    setSettings(merged);
    localStorage.setItem('app-settings', JSON.stringify(merged));
  };
  return (
    <SettingsContext.Provider value={{ settings, updateSettings }}>
      {children}
    </SettingsContext.Provider>
  );
}

export const useSettings = () => useContext(SettingsContext);