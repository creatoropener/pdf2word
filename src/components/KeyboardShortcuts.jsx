import { useEffect, useRef } from 'react';
import { useKeyboardShortcuts } from '../hooks/useKeyboardShortcuts';

export default function KeyboardShortcuts() {
  const { shortcuts, setShowModal } = useKeyboardShortcuts();
  const modalRef = useRef(null);

  useEffect(() => {
    const handleEsc = (e) => e.key === 'Escape' && setShowModal(false);
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [setShowModal]);

  if (!shortcuts.showModal) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={() => setShowModal(false)}>
      <div
        ref={modalRef}
        className="bg-white dark:bg-gray-900 rounded-2xl p-6 max-w-sm w-full mx-4 shadow-xl"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="Keyboard shortcuts"
      >
        <h2 className="text-lg font-bold mb-4">Keyboard Shortcuts</h2>
        <ul className="space-y-2 text-sm">
          <li><kbd className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded">Ctrl+O</kbd> Open file dialog</li>
          <li><kbd className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded">Ctrl+Shift+C</kbd> Start conversion</li>
          <li><kbd className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded">?</kbd> Show this help</li>
        </ul>
        <button
          onClick={() => setShowModal(false)}
          className="mt-4 w-full py-2 bg-gray-100 dark:bg-gray-800 rounded-lg"
        >
          Close
        </button>
      </div>
    </div>
  );
}