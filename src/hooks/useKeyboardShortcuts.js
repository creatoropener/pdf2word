import { useEffect, useState } from 'react';

export function useKeyboardShortcuts() {
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const handler = (e) => {
      if (e.key === '?' && !e.ctrlKey && !e.metaKey) {
        setShowModal((prev) => !prev);
        e.preventDefault();
      }
      if ((e.ctrlKey || e.metaKey) && e.key === 'o') {
        e.preventDefault();
        document.querySelector('input[type="file"]')?.click();
      }
      if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'C') {
        e.preventDefault();
        window.dispatchEvent(new CustomEvent('start-conversion'));
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);

  return { shortcuts: { showModal }, setShowModal };
}