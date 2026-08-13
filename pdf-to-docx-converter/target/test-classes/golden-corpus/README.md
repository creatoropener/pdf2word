# Golden-file regression corpus

This directory is intentionally empty in the starter skeleton. It's where
the real regression safety net goes as the heuristics in `inference/` get
tuned against real documents.

## How to use this

1. Collect 20-30 real PDFs representative of what cometfile users will
   actually upload: single-column reports, PDFs with simple ruled tables,
   PDFs with images, PDFs with bullet/numbered lists, and a few edge cases
   (empty pages, very small fonts, unusual fonts, multi-column layouts you
   know will struggle).
2. For each `name.pdf`, run it through `ConvertCli` and hand-verify the
   output `name.docx` looks right:
   ```
   mvn package
   java -jar target/pdf-to-docx-converter-0.1.0-SNAPSHOT.jar name.pdf name.docx
   ```
3. Once verified, save the good DOCX next to the PDF as `name.expected.docx`.
4. Add a JUnit test (see `GoldenFileRegressionTest` in
   `src/test/java/com/cometfile/pdf2docx/golden/`) that converts each
   `name.pdf` and asserts the output matches `name.expected.docx` on the
   properties that actually matter - exact byte-for-byte DOCX comparison is
   too brittle (POI's XML output isn't byte-stable across runs or POI
   versions), so compare things like:
   - paragraph text content, in order
   - heading levels and text
   - table row/column counts and cell text
   - number of embedded images

## Important

Do NOT commit real user PDFs here if they contain sensitive or
confidential data - use synthetic documents or samples you know are
freely licensed for this purpose.
