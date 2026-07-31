export default function ProgressSection({ jobs }) {
  if (!jobs || jobs.length === 0) return null;
  return (
    <div className="space-y-4">
      {jobs.map((job) => (
        <div key={job.id} className="flex items-center gap-4">
          <span className="text-sm font-medium truncate w-40">
            {job.file.name}
          </span>
          <div className="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div
              className="h-full bg-blue-600 transition-all duration-500"
              style={{ width: `${Math.round(job.progress * 100)}%` }}
            />
          </div>
          <span className="text-xs text-gray-500 w-16 text-right">
            {job.status === 'done'
              ? 'Done'
              : job.status === 'error'
              ? 'Error'
              : `${Math.round(job.progress * 100)}%`}
          </span>
        </div>
      ))}
    </div>
  );
}