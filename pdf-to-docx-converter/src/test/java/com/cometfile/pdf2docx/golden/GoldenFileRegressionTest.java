package com.cometfile.pdf2docx.golden;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Template for the golden-file regression suite described in
 * src/test/resources/golden-corpus/README.md. Disabled until real sample
 * PDFs are added to that directory - enable once the corpus exists.
 */
class GoldenFileRegressionTest {

    @Test
    @Disabled("Add real PDFs to src/test/resources/golden-corpus/ first - see README.md there")
    void convertsCorpusPdfsMatchingExpectedStructure() {
        // TODO: iterate over golden-corpus/*.pdf, convert each with
        // PdfToDocxConverterImpl, and compare against the corresponding
        // *.expected.docx on paragraph text, heading levels, table shape
        // and image count - not raw bytes (see README.md for why).
    }
}
