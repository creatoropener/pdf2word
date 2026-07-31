const faqs = [
  {
    q: 'Is it really free?',
    a: 'Yes, completely free. No hidden fees, no premium plans.',
  },
  {
    q: 'Where are my files uploaded?',
    a: 'Nowhere. Everything happens in your browser. We never see your files.',
  },
  {
    q: 'Can I convert scanned PDFs?',
    a: 'Yes, enable OCR (Optical Character Recognition) in the options panel.',
  },
  {
    q: 'What about large files?',
    a: 'Files up to 100MB are supported. Very large PDFs may take longer but won’t crash.',
  },
];

export default function FAQ() {
  return (
    <section className="space-y-4">
      <h2 className="text-2xl font-bold">Frequently Asked Questions</h2>
      {faqs.map(({ q, a }) => (
        <details key={q} className="group border border-gray-200 dark:border-gray-700 rounded-xl p-4">
          <summary className="font-medium cursor-pointer">{q}</summary>
          <p className="mt-2 text-gray-600 dark:text-gray-400">{a}</p>
        </details>
      ))}
    </section>
  );
}