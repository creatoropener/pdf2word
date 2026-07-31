export default function FileList({ files, onRemove, onClear }) {
  if (!files || files.length === 0) return null;
  return (
    <div className="space-y-2">
      {files.map((f) => (
        <div
          key={f.name}
          className="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 rounded"
        >
          <span className="text-sm truncate">{f.name}</span>
          <span className="text-xs text-gray-400">
            {(f.size / 1024 / 1024).toFixed(1)} MB
          </span>
          <button
            onClick={() => onRemove(f.name)}
            className="text-red-500 hover:text-red-700 text-sm ml-2"
            aria-label={`Remove ${f.name}`}
          >
            ✕
          </button>
        </div>
      ))}
      {files.length > 1 && (
        <button
          onClick={onClear}
          className="text-xs text-gray-500 underline mt-1"
        >
          Clear all
        </button>
      )}
    </div>
  );
}