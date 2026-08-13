# pdf-to-docx-converter

Standalone module that converts text-based (non-scanned) PDF documents to
Word `.docx` files, preserving paragraph, heading, list, table and image
structure. Built to be developed and tested completely on its own, then
added to **cometfile** as a plain Maven dependency once it's solid.

Scanned/image-only PDFs are explicitly out of scope (per the plan to handle
those through OCR separately) - this module assumes selectable text.

## Architecture

```
PDF bytes
   │
   ▼
extraction/     PdfExtractor, PositionedTextStripper, ImageExtractor
   │             (PDFBox) -> per-character position/font/size + placed images
   ▼
inference/      LineBuilder -> LineClassifier -> TableInference -> StructureInferenceEngine
   │             raw characters -> lines -> paragraphs/headings/lists/tables (Block tree)
   ▼
model/          DocumentModel, PageModel, Block (Paragraph/Heading/List/Table/Image)
   │             framework-agnostic intermediate representation
   ▼
generation/     DocxGenerator
   │             (POI) walks the Block tree -> .docx bytes
   ▼
api/            PdfToDocxConverter (interface) / PdfToDocxConverterImpl
                  the only thing a caller needs to depend on
```

The `model/` package is the seam: extraction and inference produce it,
generation only reads from it. Nothing in `generation/` knows PDFBox
exists, and nothing in `extraction/`/`inference/` knows POI exists. That
means you can rework the table-detection heuristic, for example, without
touching the DOCX-writing code at all, and vice versa.

## Building and running standalone

```bash
mvn clean test              # run unit + integration tests
mvn clean package           # build target/pdf-to-docx-converter-0.1.0-SNAPSHOT.jar (with deps, via shade)
java -jar target/pdf-to-docx-converter-0.1.0-SNAPSHOT.jar input.pdf output.docx
```

> **Note on this skeleton:** it was written in an environment without
> access to Maven Central, so it has **not** been compiled or run here.
> The code follows standard PDFBox 3.x / POI 5.x APIs, but run
> `mvn clean test` yourself as the first step before relying on it -
> treat this as a reviewed-but-unverified starting point, not a build
> that's already been proven green.

## What's implemented vs. stubbed

| Area | Status |
|---|---|
| Text + font/position extraction | Implemented |
| Image extraction with placement | Implemented |
| Paragraph grouping | Implemented, basic gap-based heuristic |
| Heading detection | Implemented, relative-font-size heuristic |
| List detection (bullet/numbered) | Implemented, common bullet glyphs + `1.`/`a)` patterns |
| Table detection | Implemented but weakest heuristic by far - see `TableInference` javadoc. Works on simple evenly-spaced tables; will miss borderless/irregular ones |
| Multi-column page layouts | Not handled - lines are read in raw top-to-bottom order, so multi-column text will interleave incorrectly |
| Headers/footers/footnotes | Not distinguished from body text |
| Golden-file regression suite | Scaffolded (`src/test/resources/golden-corpus/`), empty - needs real sample PDFs added |

## Iterating on the heuristics

`LineBuilder`, `LineClassifier`, and `TableInference` are each independently
unit-testable (see `LineBuilderTest` for the pattern - construct
`CharacterInfo` objects directly, no PDF needed). Build out the golden-file
corpus early (see its README) so heuristic changes get checked against real
documents automatically, not just hand-verified one PDF at a time.

Expect most of your iteration time to go into `TableInference` - it's
called out deliberately in its own javadoc as the part most likely to need
rework once you throw real-world tables at it.

## Integrating into cometfile

Once this module is solid, add it as a dependency rather than merging the
code directly into cometfile:

**Option A - local multi-module Maven build** (simplest if cometfile is
also Maven-based and in the same monorepo/build):
```xml
<module>pdf-to-docx-converter</module>
```
in cometfile's parent `pom.xml`, then in cometfile's own module:
```xml
<dependency>
  <groupId>com.cometfile</groupId>
  <artifactId>pdf-to-docx-converter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Option B - published artifact** (if cometfile lives in a separate repo):
publish this module to your internal artifact repository and depend on it
like any other library.

Either way, cometfile only ever needs to know about
`com.cometfile.pdf2docx.api.PdfToDocxConverter`:

```java
@Service
public class DocumentConversionService {

    private final PdfToDocxConverter converter = new PdfToDocxConverterImpl();
    // or, if you want it Spring-managed:
    // @Bean PdfToDocxConverter pdfToDocxConverter() { return new PdfToDocxConverterImpl(); }

    public byte[] convertPdfToDocx(MultipartFile pdfFile) throws ConversionException, IOException {
        try (InputStream in = pdfFile.getInputStream()) {
            return converter.convert(in);
        }
    }
}
```

`PdfToDocxConverterImpl` is stateless and safe to hold as a singleton bean.

If conversions need to run async for large PDFs, wrap the call in
cometfile's own job/queue infrastructure - this module's API is
intentionally synchronous and dependency-free so it doesn't presume how
cometfile wants to schedule work.
