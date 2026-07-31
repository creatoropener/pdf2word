import { useEffect } from 'react';
import { useRecentConversions } from '../hooks/useRecentConversions';

export default function RecentConversions({ jobs }) {
  const { recent, addEntry } = useRecentConversions();

  useEffect(() => {
    jobs.forEach((job) => {
      if (job.status === 'done') {
        addEntry({
          name: job.file.name,
          size: job.file.size,
          pages: job.pages || 0,
          timestamp: Date.now(),
        });
      }
    });
  }, [jobs, addEntry]);

  if (recent.length === 0) return null;

  return (
    <section className="space-y-4">
      <h2 className="text-2xl font-bold">Recent Conversions</h2>
      <div className="divide-y divide-gray-200 dark:divide-gray-800">
        {recent.slice(0, 5).map((entry, i) => (
          <div key={i} className="flex justify-between py-2 text-sm">
            <span className="font-medium">{entry.name}</span>
            <span className="text-gray-500">{new Date(entry.timestamp).toLocaleDateString()}</span>
          </div>
        ))}
      </div>
    </section>
  );
}