const features = [
  { title: 'Drag & Drop', desc: 'Upload PDFs by dragging, browsing, or pasting.' },
  { title: 'Batch Conversion', desc: 'Convert multiple files at once.' },
  { title: 'OCR Support', desc: 'Extract text from scanned pages using Tesseract.' },
  { title: 'Preserves Formatting', desc: 'Headings, lists, bold, italics, images and more.' },
  { title: 'Lightning Fast', desc: 'Web Workers keep the UI responsive.' },
  { title: 'Dark Mode', desc: 'Easy on the eyes, day or night.' },
];

export default function FeatureCards() {
  return (
    <section className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {features.map(({ title, desc }) => (
        <div
          key={title}
          className="p-6 bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-md transition"
        >
          <h3 className="text-lg font-semibold">{title}</h3>
          <p className="text-gray-600 dark:text-gray-400 text-sm mt-2">{desc}</p>
        </div>
      ))}
    </section>
  );
}