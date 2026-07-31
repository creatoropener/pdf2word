import { useState, useCallback } from 'react';
import { validateFile } from '../utils/validators';
import { useSettings } from './useSettings';

export function useFileUpload() {
  const [files, setFiles] = useState([]);
  const [errors, setErrors] = useState([]);
  const { settings } = useSettings();

  const addFiles = useCallback(
    (newFiles) => {
      const valid = [];
      const errs = [];
      for (const file of newFiles) {
        const error = validateFile(file, settings.maxFileSize);
        if (error) errs.push({ file: file.name, error });
        else valid.push(file);
      }
      setFiles((prev) => [...prev, ...valid]);
      setErrors((prev) => [...prev, ...errs]);
      return errs;
    },
    [settings.maxFileSize]
  );

  const removeFile = useCallback((name) => {
    setFiles((prev) => prev.filter((f) => f.name !== name));
  }, []);

  const clearFiles = useCallback(() => {
    setFiles([]);
    setErrors([]);
  }, []);

  return { files, errors, addFiles, removeFile, clearFiles };
}