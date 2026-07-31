export function validateFile(file, maxSize) {
  if (!file || file.size === 0) return 'File is empty.';
  if (file.type !== 'application/pdf') return 'Only PDF files are allowed.';
  if (file.size > maxSize) return `File size exceeds ${Math.round(maxSize / 1024 / 1024)} MB limit.`;
  return null;
}