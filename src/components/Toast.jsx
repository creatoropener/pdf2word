export default function Toast({ id, message, type = 'info', onClose }) {
  const bgColor = {
    info: 'bg-blue-500',
    error: 'bg-red-500',
    warning: 'bg-yellow-500',
    success: 'bg-green-500',
  }[type];
  return (
    <div className={`${bgColor} text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 animate-slide-up`}>
      <span>{message}</span>
      <button onClick={onClose} className="ml-auto">✕</button>
    </div>
  );
}